package com.axiang.ax_pro.service;

import com.axiang.ax_pro.rule.RuleEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ScoringServiceTest {
    /** 评分流程应返回非空结果代码 */
    @Test
    void scoreReturnsCode() {
        TestService testService = new TestService();
        RuleEngine ruleEngine = new RuleEngine();
        ScoringService scoringService = new ScoringService(testService, ruleEngine);

        Map<Long, Long> answers = new HashMap<>();
        answers.put(101L, 1001L);
        answers.put(102L, 2001L);

        String code = scoringService.score(1L, answers);
        Assertions.assertNotNull(code);
    }
}
