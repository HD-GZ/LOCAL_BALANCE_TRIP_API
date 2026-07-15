ALTER TABLE incentives
    ADD COLUMN description VARCHAR(200) NULL;

CREATE TABLE incentive_regions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    incentive_id BIGINT NOT NULL,
    ldong_regn_cd VARCHAR(2) NOT NULL,
    ldong_signgu_cd VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_incentive_regions UNIQUE (incentive_id, ldong_regn_cd, ldong_signgu_cd),
    CONSTRAINT fk_incentive_regions_incentive FOREIGN KEY (incentive_id)
        REFERENCES incentives (id) ON DELETE CASCADE
);
