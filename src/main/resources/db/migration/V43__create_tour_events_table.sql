CREATE TABLE tour_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    locale VARCHAR(5) NOT NULL,
    content_id VARCHAR(20) NOT NULL,
    region_candidate_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    image_url VARCHAR(500) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    address VARCHAR(300) NULL,
    event_start DATE NOT NULL,
    event_end DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tour_events_locale_content_id UNIQUE (locale, content_id),
    CONSTRAINT fk_tour_events_region_candidate FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id),
    INDEX idx_tour_events_region_active (locale, region_candidate_id, event_end, event_start)
);
