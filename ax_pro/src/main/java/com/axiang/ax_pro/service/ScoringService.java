package com.axiang.ax_pro.service;

import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import com.axiang.ax_pro.domain.TestResult;
import com.axiang.ax_pro.rule.RuleEngine;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/**
 * 评分服务
 * - 汇集题目与选项，计算维度分值
 * - 根据规则引擎生成结果代码
 * - 使用熔断与计时注解提升稳定性与可观测性
 */
public class ScoringService {
    private final TestService testService;
    private final RuleEngine ruleEngine;

    /** 构造注入依赖，便于测试与替换实现 */
    public ScoringService(TestService testService, RuleEngine ruleEngine) {
        this.testService = testService;
        this.ruleEngine = ruleEngine;
    }

    /**
     * 执行评分流程
     * 入参：测评ID与答案映射；返回：结果代码
     * 过程：加载题目与选项→计算维度分值→根据规则评估结果
     */
    @CircuitBreaker(name = "scoring")
    @Timed(value = "scoring.time")
    public String score(Long testId, Map<Long, Long> answers) {
        PersonalityTest test = testService.getTest(testId);
        List<Question> questions = testService.listQuestions(testId);
        Map<Long, Option> optionById = testService.listOptionsByTest(testId);
        Map<String, Double> dims = ruleEngine.computeDimensionsForTest(test, questions, optionById, answers);
        List<TestResult> results = testService.listResults(testId);
        String code = ruleEngine.evaluateResult(test, dims, results);
        return code;
    }
}

