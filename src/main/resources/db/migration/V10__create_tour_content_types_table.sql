CREATE TABLE tour_content_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code INT NOT NULL,
    name VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tour_content_types_code UNIQUE (code)
);

INSERT INTO tour_content_types (code, name, created_at, updated_at) VALUES
    (12, '관광지', NOW(), NOW()),
    (14, '문화시설', NOW(), NOW()),
    (28, '레포츠', NOW(), NOW()),
    (38, '쇼핑', NOW(), NOW()),
    (39, '음식점', NOW(), NOW());
