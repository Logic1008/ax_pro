package com.axiang.ax_pro.domain;

import lombok.Data;

/**
 * 测评结果定义
 * 包含结果的编码、名称、描述与匹配条件；条件可为 SpEL 表达式。
 */
@Data
public class TestResult {
    private Long id;
    private Long testId;
    private String code;
    private String name;
    private String description;
    private String condition;
}

