CREATE TABLE propensities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    locality INT NOT NULL,
    frugality INT NOT NULL,
    flexibility INT NOT NULL,
    experientiality INT NOT NULL,
    vitality INT NOT NULL,
    sociality INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_propensities_user_id UNIQUE (user_id),
    CONSTRAINT fk_propensities_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
