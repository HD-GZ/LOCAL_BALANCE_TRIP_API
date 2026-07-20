CREATE TABLE tour_places (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content_id VARCHAR(20) NOT NULL,
    ldong_regn_cd VARCHAR(2) NOT NULL,
    ldong_signgu_cd VARCHAR(3) NOT NULL,
    content_type_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    image_url VARCHAR(500) NULL,
    longitude DOUBLE NULL,
    latitude DOUBLE NULL,
    overview TEXT NULL,
    sort_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tour_places_content_id UNIQUE (content_id),
    INDEX idx_tour_places_region (ldong_regn_cd, ldong_signgu_cd, content_type_id)
);

CREATE TABLE tour_region_stats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ldong_regn_cd VARCHAR(2) NOT NULL,
    ldong_signgu_cd VARCHAR(3) NOT NULL,
    total_count INT NOT NULL,
    sample_size INT NOT NULL,
    tourist_spot_count INT NOT NULL,
    cultural_facility_count INT NOT NULL,
    leports_count INT NOT NULL,
    accommodation_count INT NOT NULL,
    shopping_count INT NOT NULL,
    restaurant_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tour_region_stats UNIQUE (ldong_regn_cd, ldong_signgu_cd)
);

CREATE TABLE odii_themes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tid VARCHAR(20) NOT NULL,
    tlid VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    longitude DOUBLE NULL,
    latitude DOUBLE NULL,
    audio_url VARCHAR(500) NULL,
    audio_synced_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_odii_themes UNIQUE (tid, tlid)
);
