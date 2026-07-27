CREATE TABLE tour_receipts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    saved_course_id BIGINT NOT NULL,
    merchant_name VARCHAR(100) NOT NULL,
    amount INT NOT NULL,
    paid_date DATE NOT NULL,
    image_key VARCHAR(300) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tour_receipts_saved_course
        FOREIGN KEY (saved_course_id) REFERENCES saved_courses (id)
);
