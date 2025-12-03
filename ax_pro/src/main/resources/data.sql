INSERT INTO personality_test (code, name, description, rule_type, rule_config) VALUES
('MBTI', 'MBTI性格测试', '基础示例', 'WEIGHTED_SUM', '{}');

INSERT INTO question (test_id, code, text, dimension) VALUES
(1, 'Q1', '我更倾向参加大型聚会', 'EI'),
(1, 'Q2', '做决定时更依赖事实', 'SN');

INSERT INTO option_item (question_id, text, value_map) VALUES
((SELECT id FROM question WHERE code='Q1'), '是', '{"EI": 1.0}'),
((SELECT id FROM question WHERE code='Q1'), '否', '{"EI": -1.0}'),
((SELECT id FROM question WHERE code='Q2'), '是', '{"SN": 1.0}'),
((SELECT id FROM question WHERE code='Q2'), '否', '{"SN": -1.0}');

INSERT INTO test_result (test_id, code, name, description, condition) VALUES
(1, 'E_S', '外向且偏实感', '示例结果', "#dim['EI'] > 0 and #dim['SN'] > 0"),
(1, 'I_N', '内向且偏直觉', '示例结果', "#dim['EI'] <= 0 and #dim['SN'] < 0");

