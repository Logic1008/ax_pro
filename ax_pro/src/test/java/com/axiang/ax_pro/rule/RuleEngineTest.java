package com.axiang.ax_pro.rule;

import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import com.axiang.ax_pro.domain.TestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RuleEngineTest {
    /** 验证加权阈值路径能命中配置并返回指定结果代码 */
    @Test
    void computeAndEvaluateWeightedSum() {
        RuleEngine engine = new RuleEngine();
        PersonalityTest test = new PersonalityTest();
        test.setRuleType("WEIGHTED_SUM");
        test.setRuleConfig("{\"weights\":{\"EI\":1.0,\"SN\":1.0},\"thresholds\":[{\"min\":0,\"max\":100,\"code\":\"CODE_A\"}]}");

        Question q = new Question();
        q.setId(1L);
        List<Question> qs = Collections.singletonList(q);

        Option o = new Option();
        o.setId(2L);
        o.setValueMap("{\"EI\":1.0,\"SN\":1.0}");
        Map<Long, Option> optMap = new HashMap<>();
        optMap.put(2L, o);

        Map<Long, Long> answers = new HashMap<>();
        answers.put(1L, 2L);

        Map<String, Double> dims = engine.computeDimensionsForTest(test, qs, optMap, answers);
        Assertions.assertEquals(1.0, dims.get("EI"));
        Assertions.assertEquals(1.0, dims.get("SN"));

        List<TestResult> rs = Collections.emptyList();
        String code = engine.evaluateResult(test, dims, rs);
        Assertions.assertEquals("CODE_A", code);
    }
}
