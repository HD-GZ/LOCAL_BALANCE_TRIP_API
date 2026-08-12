CREATE TABLE course_share_tokens (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    saved_course_id BIGINT      NOT NULL,
    token           VARCHAR(36) NOT NULL,
    expires_at      DATETIME    NOT NULL,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL,
    CONSTRAINT uk_course_share_tokens_token UNIQUE (token),
    CONSTRAINT fk_course_share_tokens_saved_course
        FOREIGN KEY (saved_course_id) REFERENCES saved_courses (id) ON DELETE CASCADE
);
