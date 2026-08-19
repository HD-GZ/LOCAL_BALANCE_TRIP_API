ALTER TABLE region_candidates
    ADD COLUMN name_en VARCHAR(100) NULL AFTER name;

ALTER TABLE recommended_regions
    ADD COLUMN locale VARCHAR(5) NOT NULL DEFAULT 'ko' AFTER user_id,
    MODIFY COLUMN region_name VARCHAR(100) NOT NULL;

ALTER TABLE saved_courses
    MODIFY COLUMN region_name VARCHAR(100) NOT NULL;
