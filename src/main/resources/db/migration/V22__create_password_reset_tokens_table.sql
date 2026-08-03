CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    code VARCHAR(6) NOT NULL,
    code_expires_at DATETIME(6) NOT NULL,
    reset_token VARCHAR(36) NULL,
    token_expires_at DATETIME(6) NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_reset_token UNIQUE (reset_token),
    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
