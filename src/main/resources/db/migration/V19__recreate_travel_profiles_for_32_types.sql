DROP TABLE travel_profiles;

CREATE TABLE travel_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code CHAR(5) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    image_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_travel_profiles_code UNIQUE (code)
);
