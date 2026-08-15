ALTER TABLE tour_region_stats
    ADD COLUMN luxury_shopping_count INT NOT NULL DEFAULT 0,
    ADD COLUMN traditional_market_count INT NOT NULL DEFAULT 0,
    ADD COLUMN viewing_place_count INT NOT NULL DEFAULT 0,
    ADD COLUMN experience_place_count INT NOT NULL DEFAULT 0,
    ADD COLUMN nature_rest_count INT NOT NULL DEFAULT 0,
    ADD COLUMN cafe_count INT NOT NULL DEFAULT 0,
    ADD COLUMN exhibition_count INT NOT NULL DEFAULT 0;

CREATE TABLE region_visitor_stats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ldong_regn_cd VARCHAR(2) NOT NULL,
    ldong_signgu_cd VARCHAR(3) NOT NULL,
    base_date DATE NOT NULL,
    visitor_type VARCHAR(20) NOT NULL,
    visitor_count DOUBLE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_region_visitor_stats UNIQUE (ldong_regn_cd, ldong_signgu_cd, base_date, visitor_type)
);

CREATE TABLE category_group_codes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_group VARCHAR(30) NOT NULL,
    code_level VARCHAR(10) NOT NULL,
    code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_group_codes UNIQUE (category_group, code_level, code)
);

INSERT INTO category_group_codes (category_group, code_level, code, created_at, updated_at) VALUES
('LUXURY_SHOPPING', 'CAT3', 'A04010300', NOW(), NOW()),
('LUXURY_SHOPPING', 'CAT3', 'A04010400', NOW(), NOW()),
('TRADITIONAL_MARKET', 'CAT3', 'A04010100', NOW(), NOW()),
('TRADITIONAL_MARKET', 'CAT3', 'A04010200', NOW(), NOW()),
('TRADITIONAL_MARKET', 'CAT3', 'A04010900', NOW(), NOW()),
('VIEWING_PLACE', 'CAT2', 'A0201', NOW(), NOW()),
('VIEWING_PLACE', 'CAT2', 'A0205', NOW(), NOW()),
('VIEWING_PLACE', 'CAT2', 'A0206', NOW(), NOW()),
('EXPERIENCE_PLACE', 'CAT2', 'A0203', NOW(), NOW()),
('EXPERIENCE_PLACE', 'CAT3', 'A04010700', NOW(), NOW()),
('NATURE_REST', 'CAT1', 'A01', NOW(), NOW()),
('NATURE_REST', 'CAT2', 'A0202', NOW(), NOW()),
('CAFE', 'CAT3', 'A05020900', NOW(), NOW()),
('EXHIBITION', 'CAT3', 'A02060100', NOW(), NOW()),
('EXHIBITION', 'CAT3', 'A02060300', NOW(), NOW()),
('EXHIBITION', 'CAT3', 'A02060500', NOW(), NOW());
