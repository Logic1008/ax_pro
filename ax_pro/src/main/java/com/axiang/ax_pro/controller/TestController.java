package com.axiang.ax_pro.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.axiang.ax_pro.common.ApiResponse;
import com.axiang.ax_pro.common.BusinessException;
import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import com.axiang.ax_pro.domain.TestResult;
import com.axiang.ax_pro.dto.SubmitRequest;
import com.axiang.ax_pro.dto.TestDetailResponse;
import com.axiang.ax_pro.service.ScoringService;
import com.axiang.ax_pro.service.StatsService;
import com.axiang.ax_pro.service.TestService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Timed;
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
 * - 列表/详情匿名可访问（详情限流）
 * - 提交需登录（限流），成功后统计 PV/UV
 * - 结果查询匿名可访问
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
    /** 列出所有测评（匿名） */
    @GetMapping
    @Timed(value = "api.tests.list")
    public ResponseEntity<ApiResponse<List<PersonalityTest>>> list() {
        return ResponseEntity.ok(ApiResponse.success(testService.listTests()));
    }

    /**
     * 获取测评详情
     * 路径：GET /api/tests/{id}
     */
    /** 获取测评详情（匿名，限流 tests-detail-rl） */
    @GetMapping("/{id}")
    @RateLimiter(name = "tests-detail-rl")
    @Timed(value = "api.tests.detail")
    public ResponseEntity<ApiResponse<TestDetailResponse>> detail(@PathVariable @Positive Long id) {
        statsService.incrPv(id);
        PersonalityTest test = testService.getTest(id);
        if (test == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "test not found"));
        }
        List<Question> qs = testService.listQuestions(id);
        Map<Long, List<Option>> ops = new HashMap<>();
        for (Question q : qs) {
            ops.put(q.getId(), testService.listOptionsByQuestion(q.getId()));
        }
        TestDetailResponse resp = new TestDetailResponse();
        resp.setTest(test);
        resp.setQuestions(qs);
        resp.setOptions(ops);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    /**
     * 提交测评答案并返回结果代码
     * 路径：POST /api/tests/{id}/submit
     */
    /** 提交答案并返回结果（需登录，限流 tests-submit-rl） */
    @PostMapping("/{id}/submit")
    @RateLimiter(name = "tests-submit-rl")
    @Timed(value = "api.tests.submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submit(@PathVariable @Positive Long id, @Validated @RequestBody SubmitRequest req) {
        PersonalityTest test = testService.getTest(id);
        if (test == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "test not found"));
        }
        if (req.getAnswers() == null || req.getAnswers().isEmpty()) {
            throw new BusinessException("BAD_REQUEST", "answers required", HttpStatus.BAD_REQUEST);
        }
        String code = scoringService.score(id, req.getAnswers());
        statsService.incrPv(id);
        if (StpUtil.isLogin()) {
            statsService.recordUv(id, String.valueOf(StpUtil.getLoginId()));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("resultCode", code);
        return ResponseEntity.ok(ApiResponse.success(map));
    }

    /**
     * 根据结果代码查询结果详情
     * 路径：GET /api/tests/{id}/result/{code}
     */
    /** 按结果代码查询结果（匿名） */
    @GetMapping("/{id}/result/{code}")
    public ResponseEntity<ApiResponse<TestResult>> result(@PathVariable @Positive Long id, @PathVariable String code) {
        TestResult r = testService.findResultByCode(id, code);
        if (r != null) {
            return ResponseEntity.ok(ApiResponse.success(r));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", "result not found"));
    }
}

