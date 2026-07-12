CREATE TABLE recommended_regions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    region_name VARCHAR(50) NOT NULL,
    image_url VARCHAR(500) NULL,
    reason VARCHAR(300) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_recommended_regions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE generated_courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recommended_region_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    reason VARCHAR(300) NOT NULL,
    image_url VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_generated_courses_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_generated_courses_region
        FOREIGN KEY (recommended_region_id) REFERENCES recommended_regions (id)
);

CREATE TABLE course_places (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    visit_order INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    overview TEXT NULL,
    image_url VARCHAR(500) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    walk_minutes INT NULL,
    has_audio BOOLEAN NOT NULL DEFAULT FALSE,
    audio_url VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_course_places_course
        FOREIGN KEY (course_id) REFERENCES generated_courses (id)
);

CREATE TABLE saved_courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_course_id BIGINT NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    region_name VARCHAR(50) NOT NULL,
    reason VARCHAR(300) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_saved_courses_user_source UNIQUE (user_id, source_course_id),
    CONSTRAINT fk_saved_courses_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE saved_course_places (
    id BIGINT NOT NULL AUTO_INCREMENT,
    saved_course_id BIGINT NOT NULL,
    visit_order INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    overview TEXT NULL,
    image_url VARCHAR(500) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    walk_minutes INT NULL,
    has_audio BOOLEAN NOT NULL DEFAULT FALSE,
    audio_url VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_saved_course_places_saved_course
        FOREIGN KEY (saved_course_id) REFERENCES saved_courses (id)
);
