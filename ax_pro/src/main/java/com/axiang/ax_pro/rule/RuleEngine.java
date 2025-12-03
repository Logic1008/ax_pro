package com.axiang.ax_pro.rule;

import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import com.axiang.ax_pro.domain.TestResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评分规则引擎
 * 支持表达式、单维区间、最近原型、加权阈值、强迫选择与分支流等模式。
 */
public class RuleEngine {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 计算维度分值（标准累加）
     */
    public Map<String, Double> computeDimensions(List<Question> questions, Map<Long, Option> optionById, Map<Long, Long> answers) {
        Map<String, Double> dims = new HashMap<>();
        answers.forEach((qid, oid) -> {
            // 根据选项ID拿到选项；若不存在则忽略该答案
            Option opt = optionById.get(oid);
            if (opt == null) return;
            // 解析选项的维度增量映射（JSON）
            Map<String, Double> valMap = parseValueMap(opt.getValueMap());
            // 累加到各维度分值
            valMap.forEach((k, v) -> dims.put(k, dims.getOrDefault(k, 0.0) + v));
        });
        return dims;
    }

    /**
     * 按测试类型计算维度分值
     */
    public Map<String, Double> computeDimensionsForTest(PersonalityTest test, List<Question> questions, Map<Long, Option> optionById, Map<Long, Long> answers) {
        RuleType type = getRuleType(test);
        // IPSATIVE 模式采用零和化的相对分配
        if (type == RuleType.IPSATIVE) return computeDimensionsIpsative(questions, optionById, answers);
        // 其他模式沿用标准累加
        return computeDimensions(questions, optionById, answers);
    }

    /**
     * 评估并返回结果代码
     */
    public String evaluateResult(PersonalityTest test, Map<String, Double> dims, List<TestResult> results) {
        RuleType type = getRuleType(test);
        // 直接表达式匹配
        if (type == RuleType.EXPRESSION) return evaluateByExpression(dims, results);
        // 单维区间映射
        if (type == RuleType.SINGLE_DIM_INTERVAL) return evaluateBySingleDimInterval(test, dims);
        // 最近原型匹配
        if (type == RuleType.NEAREST_TEMPLATE) return evaluateByNearestTemplate(test, dims);
        if (type == RuleType.WEIGHTED_SUM) {
            // 先进行加权阈值映射，失败则回退到表达式匹配
            String v = evaluateByWeightedSum(test, dims);
            if (v != null) return v;
            return evaluateByExpression(dims, results);
        }
        // 分支流：命中条件后评估子规则
        if (type == RuleType.FLOW_BRANCH) return evaluateByFlowBranch(test, dims);
        return evaluateByExpression(dims, results);
    }

    /**
     * 执行 SpEL 条件表达式
     */
    private boolean evalCondition(String condition, Map<String, Double> dims) {
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        // 将维度分值注入为 SpEL 变量 dim
        ctx.setVariable("dim", dims);
        // 解析并计算表达式
        Expression exp = parser.parseExpression(condition);
        Object v = exp.getValue(ctx);
        // 仅当表达式结果为布尔时返回匹配
        if (v instanceof Boolean) return (Boolean) v;
        return false;
    }

    /**
     * 表达式匹配模式
     */
    private String evaluateByExpression(Map<String, Double> dims, List<TestResult> results) {
        for (TestResult r : results) {
            // 没有条件的结果跳过
            if (r.getCondition() == null || r.getCondition().isEmpty()) continue;
            // 命中条件返回该结果代码
            if (evalCondition(r.getCondition(), dims)) return r.getCode();
        }
        return null;
    }

    /**
     * 强迫选择（Ipsative）维度计算：对选项维度值进行零和化调整
     */
    private Map<String, Double> computeDimensionsIpsative(List<Question> questions, Map<Long, Option> optionById, Map<Long, Long> answers) {
        Map<String, Double> dims = new HashMap<>();
        answers.forEach((qid, oid) -> {
            Option opt = optionById.get(oid);
            if (opt == null) return;
            Map<String, Double> valMap = parseValueMap(opt.getValueMap());
            if (valMap.isEmpty()) return;
            // 计算该选项各维度增量之和与均值
            double sum = 0.0;
            for (Double v : valMap.values()) sum += v;
            double avg = sum / valMap.size();
            // 对每个维度进行零和化调整（值-均值）
            for (Map.Entry<String, Double> e : valMap.entrySet()) {
                double adj = e.getValue() - avg;
                dims.put(e.getKey(), dims.getOrDefault(e.getKey(), 0.0) + adj);
            }
        });
        return dims;
    }

    /**
     * 单维度区间映射模式
     */
    private String evaluateBySingleDimInterval(PersonalityTest test, Map<String, Double> dims) {
        Map<String, Object> cfg = parseConfig(test.getRuleConfig());
        if (cfg == null) return null;
        Object d = cfg.get("dimension");
        Object intervals = cfg.get("intervals");
        // 配置缺失直接返回空
        if (!(d instanceof String) || !(intervals instanceof List)) return null;
        double val = dims.getOrDefault((String) d, 0.0);
        for (Object o : (List<?>) intervals) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) o;
            Double min = toDouble(m.get("min"));
            Double max = toDouble(m.get("max"));
            Object code = m.get("code");
            // 检查区间与结果代码合法性
            if (min == null || max == null || !(code instanceof String)) continue;
            if (val >= min && val < max) return (String) code;
        }
        return null;
    }

    /**
     * 最近原型匹配模式（欧氏距离）
     */
    private String evaluateByNearestTemplate(PersonalityTest test, Map<String, Double> dims) {
        Map<String, Object> cfg = parseConfig(test.getRuleConfig());
        if (cfg == null) return null;
        Object templates = cfg.get("templates");
        if (!(templates instanceof List)) return null;
        String bestCode = null;
        Double bestDist = null;
        for (Object t : (List<?>) templates) {
            if (!(t instanceof Map)) continue;
            Map<?, ?> tm = (Map<?, ?>) t;
            Object code = tm.get("code");
            Object vector = tm.get("vector");
            if (!(code instanceof String) || !(vector instanceof Map)) continue;
            // 计算当前模板与实际维度的欧氏距离
            double dist = 0.0;
            Map<?, ?> vec = (Map<?, ?>) vector;
            for (Map.Entry<?, ?> e : vec.entrySet()) {
                if (!(e.getKey() instanceof String)) continue;
                double target = toDouble(e.getValue());
                double actual = dims.getOrDefault((String) e.getKey(), 0.0);
                double diff = actual - target;
                dist += diff * diff;
            }
            if (bestDist == null || dist < bestDist) {
                bestDist = dist;
                bestCode = (String) code;
            }
        }
        return bestCode;
    }

    /**
     * 多维加权累加与阈值映射模式
     */
    private String evaluateByWeightedSum(PersonalityTest test, Map<String, Double> dims) {
        Map<String, Object> cfg = parseConfig(test.getRuleConfig());
        if (cfg == null) return null;
        Object weightsObj = cfg.get("weights");
        Object thresholdsObj = cfg.get("thresholds");
        if (!(weightsObj instanceof Map) || !(thresholdsObj instanceof List)) return null;
        Map<?, ?> weights = (Map<?, ?>) weightsObj;
        // 按权重累加得到总分
        double score = 0.0;
        for (Map.Entry<?, ?> e : weights.entrySet()) {
            if (!(e.getKey() instanceof String)) continue;
            double w = toDouble(e.getValue());
            score += dims.getOrDefault((String) e.getKey(), 0.0) * w;
        }
        for (Object o : (List<?>) thresholdsObj) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) o;
            Double min = toDouble(m.get("min"));
            Double max = toDouble(m.get("max"));
            Object code = m.get("code");
            // 总分落入某区间则返回对应结果
            if (min == null || max == null || !(code instanceof String)) continue;
            if (score >= min && score < max) return (String) code;
        }
        return null;
    }

    /**
     * 分段测试/流程分支模式：命中条件后评估子规则
     */
    private String evaluateByFlowBranch(PersonalityTest test, Map<String, Double> dims) {
        Map<String, Object> cfg = parseConfig(test.getRuleConfig());
        if (cfg == null) return null;
        Object segments = cfg.get("segments");
        if (!(segments instanceof List)) return null;
        for (Object s : (List<?>) segments) {
            if (!(s instanceof Map)) continue;
            Map<?, ?> sm = (Map<?, ?>) s;
            Object cond = sm.get("condition");
            Object subRule = sm.get("subRule");
            if (!(cond instanceof String) || !(subRule instanceof Map)) continue;
            // 条件未命中则继续下一个分支
            if (!evalCondition((String) cond, dims)) continue;
            Map<?, ?> sr = (Map<?, ?>) subRule;
            Object typeObj = sr.get("type");
            Object confObj = sr.get("config");
            if (!(typeObj instanceof String) || !(confObj instanceof Map)) continue;
            PersonalityTest nested = new PersonalityTest();
            nested.setRuleType((String) typeObj);
            try {
                // 将嵌套配置转为 JSON，递归评估子规则
                String json = objectMapper.writeValueAsString(confObj);
                nested.setRuleConfig(json);
            } catch (Exception ignored) {}
            return evaluateResult(nested, dims, null);
        }
        return null;
    }

    /**
     * 解析规则配置 JSON 为 Map
     */
    private Map<String, Object> parseConfig(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            // 解析失败返回空，调用方将进行回退处理
            return null;
        }
    }

    /**
     * 安全地将对象转换为 Double
     */
    private Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            // 非数字内容返回 null，调用方需判空
            return null;
        }
    }

    /**
     * 获取规则类型，异常时回退为 WEIGHTED_SUM
     */
    private RuleType getRuleType(PersonalityTest test) {
        try {
            return RuleType.valueOf(String.valueOf(test.getRuleType()));
        } catch (Exception e) {
            // 未配置或非法类型时回退到 WEIGHTED_SUM
            return RuleType.WEIGHTED_SUM;
        }
    }

    /**
     * 解析选项的维度增量 JSON
     */
    private Map<String, Double> parseValueMap(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>(){});
        } catch (Exception e) {
            // 解析失败回退为空映射，避免影响其他维度计算
            return new HashMap<>();
        }
    }
}

