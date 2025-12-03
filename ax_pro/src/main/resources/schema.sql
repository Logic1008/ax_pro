CREATE TABLE IF NOT EXISTS personality_test (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64),
  name VARCHAR(255),
  description TEXT,
  rule_type VARCHAR(64),
  rule_config TEXT
);

CREATE TABLE IF NOT EXISTS question (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  test_id BIGINT,
  code VARCHAR(64),
  text VARCHAR(500),
  dimension VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS option_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  question_id BIGINT,
  text VARCHAR(500),
  value_map TEXT
);

CREATE TABLE IF NOT EXISTS test_result (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  test_id BIGINT,
  code VARCHAR(64),
  name VARCHAR(255),
  description TEXT,
  condition TEXT
);

