ALTER TABLE images
    ADD COLUMN uploader_id BIGINT NULL,
    ADD COLUMN storage_directory VARCHAR(30) NULL;

UPDATE images i
JOIN saved_courses sc ON sc.id = i.saved_course_id
SET
    i.uploader_id = sc.user_id,
    i.storage_directory = 'RECEIPT';

UPDATE images
SET status = 'TEMPORARY'
WHERE status = 'PENDING';

UPDATE images
SET content_type = 'application/octet-stream'
WHERE content_type IS NULL;

ALTER TABLE images
    MODIFY COLUMN uploader_id BIGINT NOT NULL,
    MODIFY COLUMN storage_directory VARCHAR(30) NOT NULL,
    MODIFY COLUMN content_type VARCHAR(50) NOT NULL,
    ADD CONSTRAINT fk_images_uploader
        FOREIGN KEY (uploader_id) REFERENCES users (id);

ALTER TABLE images
    DROP FOREIGN KEY fk_images_saved_course,
    DROP COLUMN saved_course_id,
    DROP COLUMN purpose;
