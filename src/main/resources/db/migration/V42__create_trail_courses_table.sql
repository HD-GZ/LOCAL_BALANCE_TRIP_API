CREATE TABLE trail_courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    crs_idx VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    brd_div VARCHAR(20) NULL,
    route_idx VARCHAR(50) NULL,
    distance_km DECIMAL(6, 2) NULL,
    required_minutes INT NULL,
    level INT NULL,
    sigun VARCHAR(100) NULL,
    gpx_path VARCHAR(500) NULL,
    summary TEXT NULL,
    region_candidate_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_trail_courses_crs_idx UNIQUE (crs_idx),
    CONSTRAINT fk_trail_courses_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id)
);

CREATE INDEX idx_trail_courses_region_candidate ON trail_courses (region_candidate_id, distance_km);
