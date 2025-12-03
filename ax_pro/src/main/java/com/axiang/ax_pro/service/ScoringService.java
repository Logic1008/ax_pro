package com.axiang.ax_pro.service;

import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import com.axiang.ax_pro.domain.TestResult;
import com.axiang.ax_pro.rule.RuleEngine;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/**
 * 评分服务
 * 负责汇集题目与选项，计算维度分值并根据规则引擎生成结果代码。
 */
public class ScoringService {
    private final TestService testService;
    private final RuleEngine ruleEngine = new RuleEngine();

    public ScoringService(TestService testService) {
        this.testService = testService;
    }

    /**
     * 执行评分流程
     * 入参：测评ID与答案映射；返回：结果代码
     */
    public String score(Long testId, Map<Long, Long> answers) {
        PersonalityTest test = testService.getTest(testId);
        List<Question> questions = testService.listQuestions(testId);
        Map<Long, Option> optionById = new HashMap<>();
        for (Question q : questions) {
            List<Option> opts = testService.listOptionsByQuestion(q.getId());
            opts.forEach(o -> optionById.put(o.getId(), o));
        }
        Map<String, Double> dims = ruleEngine.computeDimensionsForTest(test, questions, optionById, answers);
        List<TestResult> results = testService.listResults(testId);
        String code = ruleEngine.evaluateResult(test, dims, results);
        return code;
    }
}

