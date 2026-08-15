CREATE TABLE images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    saved_course_id BIGINT NOT NULL,
    purpose VARCHAR(20) NOT NULL,
    storage_key VARCHAR(300) NOT NULL,
    content_type VARCHAR(50) NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_images_storage_key UNIQUE (storage_key),
    CONSTRAINT fk_images_saved_course
        FOREIGN KEY (saved_course_id) REFERENCES saved_courses (id)
);

INSERT INTO images (
    saved_course_id,
    purpose,
    storage_key,
    content_type,
    file_size,
    status,
    created_at,
    updated_at
)
SELECT
    saved_course_id,
    'RECEIPT',
    image_key,
    NULL,
    0,
    'ATTACHED',
    created_at,
    updated_at
FROM tour_receipts;

ALTER TABLE tour_receipts
    ADD COLUMN image_id BIGINT NULL;

UPDATE tour_receipts tr
JOIN images i ON i.storage_key = tr.image_key
SET tr.image_id = i.id;

ALTER TABLE tour_receipts
    MODIFY COLUMN image_id BIGINT NOT NULL,
    ADD CONSTRAINT uk_tour_receipts_image UNIQUE (image_id),
    ADD CONSTRAINT fk_tour_receipts_image
        FOREIGN KEY (image_id) REFERENCES images (id),
    DROP COLUMN image_key;
