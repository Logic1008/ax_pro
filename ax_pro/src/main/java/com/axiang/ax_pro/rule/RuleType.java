package com.axiang.ax_pro.rule;

/**
 * 评分规则类型枚举
 * 标识评分引擎支持的多种模式。
 */
public enum RuleType {
    WEIGHTED_SUM,
    EXPRESSION,
    SINGLE_DIM_INTERVAL,
    NEAREST_TEMPLATE,
    IPSATIVE,
    FLOW_BRANCH
}

