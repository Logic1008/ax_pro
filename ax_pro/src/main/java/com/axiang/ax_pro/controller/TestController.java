package com.axiang.ax_pro.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import com.axiang.ax_pro.domain.TestResult;
import com.axiang.ax_pro.dto.SubmitRequest;
import com.axiang.ax_pro.dto.TestDetailResponse;
import com.axiang.ax_pro.service.ScoringService;
import com.axiang.ax_pro.service.StatsService;
import com.axiang.ax_pro.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
/**
 * 测评控制器
 * 提供测评列表、详情、提交评分与按代码查询结果等接口。
 */
public class TestController {
    private final TestService testService;
    private final ScoringService scoringService;
    private final StatsService statsService;

    public TestController(TestService testService, ScoringService scoringService, StatsService statsService) {
        this.testService = testService;
        this.scoringService = scoringService;
        this.statsService = statsService;
    }

    /**
     * 列出所有测评
     * 路径：GET /api/tests
     */
    @GetMapping
    public List<PersonalityTest> list() {
        return testService.listTests();
    }

    /**
     * 获取测评详情
     * 路径：GET /api/tests/{id}
     */
    @GetMapping("/{id}")
    public TestDetailResponse detail(@PathVariable Long id) {
        statsService.incrPv(id);
        PersonalityTest test = testService.getTest(id);
        List<Question> qs = testService.listQuestions(id);
        Map<Long, List<Option>> ops = new HashMap<>();
        for (Question q : qs) {
            ops.put(q.getId(), testService.listOptionsByQuestion(q.getId()));
        }
        TestDetailResponse resp = new TestDetailResponse();
        resp.setTest(test);
        resp.setQuestions(qs);
        resp.setOptions(ops);
        return resp;
    }

    /**
     * 提交测评答案并返回结果代码
     * 路径：POST /api/tests/{id}/submit
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submit(@PathVariable Long id, @Validated @RequestBody SubmitRequest req) {
        String code = scoringService.score(id, req.getAnswers());
        statsService.incrPv(id);
        if (StpUtil.isLogin()) statsService.recordUv(id, String.valueOf(StpUtil.getLoginId()));
        Map<String, Object> map = new HashMap<>();
        map.put("resultCode", code);
        return ResponseEntity.ok(map);
    }

    /**
     * 根据结果代码查询结果详情
     * 路径：GET /api/tests/{id}/result/{code}
     */
    @GetMapping("/{id}/result/{code}")
    public ResponseEntity<TestResult> result(@PathVariable Long id, @PathVariable String code) {
        List<TestResult> list = testService.listResults(id);
        for (TestResult r : list) {
            if (code.equals(r.getCode())) return ResponseEntity.ok(r);
        }
        return ResponseEntity.notFound().build();
    }
}

