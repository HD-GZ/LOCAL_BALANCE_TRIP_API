CREATE TABLE region_green_metrics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    region_candidate_id BIGINT NOT NULL,
    gpx_adjacent BOOLEAN NOT NULL DEFAULT FALSE,
    transit_accessible BOOLEAN NOT NULL DEFAULT FALSE,
    local_commerce BOOLEAN NOT NULL DEFAULT FALSE,
    tourism_card_merchant BOOLEAN NOT NULL DEFAULT FALSE,
    population_decline BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_region_green_metrics_candidate UNIQUE (region_candidate_id),
    CONSTRAINT fk_region_green_metrics_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id)
);
