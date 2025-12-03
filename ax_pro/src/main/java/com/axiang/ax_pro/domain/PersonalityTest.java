package com.axiang.ax_pro.domain;

import lombok.Data;

/**
 * 测评定义
 * 描述一次性格测评的元信息与评分规则。
 * ruleType 指定评分模式，ruleConfig 为对应模式的 JSON 配置。
 */
@Data
public class PersonalityTest {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String ruleType;
    private String ruleConfig;
}

