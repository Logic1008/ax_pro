package com.axiang.ax_pro.dto;

import com.axiang.ax_pro.domain.Option;
import com.axiang.ax_pro.domain.PersonalityTest;
import com.axiang.ax_pro.domain.Question;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 测评详情响应
 * 包含测评元信息、题目列表与题目对应的选项集合。
 */
@Data
public class TestDetailResponse {
    private PersonalityTest test;
    private List<Question> questions;
    private Map<Long, List<Option>> options;
}

