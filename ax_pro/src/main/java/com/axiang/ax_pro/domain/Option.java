package com.axiang.ax_pro.domain;

import lombok.Data;

/**
 * 选项实体
 * 用于描述某题目的一个可选项及其对各维度的分值影响。
 * valueMap 字段保存为 JSON（如 {"EI":1.0}），表示维度增量。
 */
@Data
public class Option {
    private Long id;
    private Long questionId;
    private String text;
    private String valueMap;
}

