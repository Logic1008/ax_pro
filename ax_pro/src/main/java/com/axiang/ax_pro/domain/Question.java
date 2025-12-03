package com.axiang.ax_pro.domain;

import lombok.Data;

/**
 * 题目实体
 * 描述所属测评、题目编码与文案，以及归属维度（用于分值聚合）。
 */
@Data
public class Question {
    private Long id;
    private Long testId;
    private String code;
    private String text;
    private String dimension;
}

