package com.axiang.ax_pro.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * 测评提交请求
 * answers 映射题目ID到选项ID，用于评分计算。
 */
@Data
public class SubmitRequest {
    @NotEmpty
    private Map<Long, Long> answers;
}

