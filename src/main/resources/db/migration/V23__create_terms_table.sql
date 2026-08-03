CREATE TABLE terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    effective_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_terms_type_version UNIQUE (type, version),
    INDEX idx_terms_type_effective_date (type, effective_date)
);
