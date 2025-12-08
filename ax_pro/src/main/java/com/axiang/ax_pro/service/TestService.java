package com.axiang.ax_pro.service;

import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import com.axiang.ax_pro.domain.TestResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * 测评数据服务（内存示例）
 * 提供测评、题目、选项与结果的只读访问，便于演示与开发。
 */
public class TestService {
    private final Map<Long, PersonalityTest> tests = new HashMap<>();
    private final Map<Long, List<Question>> questions = new HashMap<>();
    private final Map<Long, List<Option>> options = new HashMap<>();
    private final Map<Long, List<TestResult>> results = new HashMap<>();

    public TestService() {
        PersonalityTest t = new PersonalityTest();
        t.setId(1L);
        t.setCode("MBTI");
        t.setName("MBTI性格测试");
        t.setDescription("基础示例");
        t.setRuleType("WEIGHTED_SUM");
        t.setRuleConfig("{}");
        tests.put(1L, t);

        List<Question> ql = new ArrayList<>();
        Question q1 = new Question();
        q1.setId(101L);
        q1.setTestId(1L);
        q1.setCode("Q1");
        q1.setText("我更倾向参加大型聚会");
        q1.setDimension("EI");
        ql.add(q1);
        Question q2 = new Question();
        q2.setId(102L);
        q2.setTestId(1L);
        q2.setCode("Q2");
        q2.setText("做决定时更依赖事实");
        q2.setDimension("SN");
        ql.add(q2);
        questions.put(1L, Collections.unmodifiableList(ql));

        List<Option> o1 = new ArrayList<>();
        Option o11 = new Option();
        o11.setId(1001L);
        o11.setQuestionId(101L);
        o11.setText("是");
        o11.setValueMap("{\"EI\":1.0}");
        o1.add(o11);
        Option o12 = new Option();
        o12.setId(1002L);
        o12.setQuestionId(101L);
        o12.setText("否");
        o12.setValueMap("{\"EI\":-1.0}");
        o1.add(o12);
        options.put(101L, Collections.unmodifiableList(o1));

        List<Option> o2 = new ArrayList<>();
        Option o21 = new Option();
        o21.setId(2001L);
        o21.setQuestionId(102L);
        o21.setText("是");
        o21.setValueMap("{\"SN\":1.0}");
        o2.add(o21);
        Option o22 = new Option();
        o22.setId(2002L);
        o22.setQuestionId(102L);
        o22.setText("否");
        o22.setValueMap("{\"SN\":-1.0}");
        o2.add(o22);
        options.put(102L, Collections.unmodifiableList(o2));

        List<TestResult> rl = new ArrayList<>();
        TestResult r1 = new TestResult();
        r1.setId(3001L);
        r1.setTestId(1L);
        r1.setCode("E_S");
        r1.setName("外向且偏实感");
        r1.setDescription("示例结果");
        r1.setCondition("#dim['EI'] > 0 and #dim['SN'] > 0");
        rl.add(r1);
        TestResult r2 = new TestResult();
        r2.setId(3002L);
        r2.setTestId(1L);
        r2.setCode("I_N");
        r2.setName("内向且偏直觉");
        r2.setDescription("示例结果");
        r2.setCondition("#dim['EI'] <= 0 and #dim['SN'] < 0");
        rl.add(r2);
        results.put(1L, Collections.unmodifiableList(rl));
    }

    /**
     * 列出所有测评
     */
    public List<PersonalityTest> listTests() {
        return new ArrayList<>(tests.values());
    }

    /**
     * 获取指定测评
     */
    public PersonalityTest getTest(Long id) {
        return tests.get(id);
    }

    /**
     * 获取测评下的题目集合
     */
    public List<Question> listQuestions(Long testId) {
        List<Question> qs = questions.get(testId);
        return qs != null ? qs : Collections.emptyList();
    }

    /**
     * 获取题目的选项集合
     */
    public List<Option> listOptionsByQuestion(Long questionId) {
        List<Option> ops = options.get(questionId);
        return ops != null ? ops : Collections.emptyList();
    }

    /**
     * 批量加载测评下所有选项并按选项ID构建映射
     */
    public Map<Long, Option> listOptionsByTest(Long testId) {
        List<Question> qs = listQuestions(testId);
        Map<Long, Option> map = new HashMap<>();
        for (Question q : qs) {
            for (Option o : listOptionsByQuestion(q.getId())) {
                map.put(o.getId(), o);
            }
        }
        return map;
    }

    /**
     * 获取测评的结果集合
     */
    public List<TestResult> listResults(Long testId) {
        List<TestResult> rs = results.get(testId);
        return rs != null ? rs : Collections.emptyList();
    }

    /**
     * 根据结果代码查询结果
     */
    public TestResult findResultByCode(Long testId, String code) {
        for (TestResult r : listResults(testId)) {
            if (code != null && code.equals(r.getCode())) {
                return r;
            }
        }
        return null;
    }
}

