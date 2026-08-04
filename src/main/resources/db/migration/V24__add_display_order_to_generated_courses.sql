ALTER TABLE generated_courses
    ADD COLUMN display_order INT NULL;

UPDATE generated_courses gc
JOIN (
    SELECT
        id,
        ROW_NUMBER() OVER (PARTITION BY recommended_region_id ORDER BY id) AS display_order
    FROM generated_courses
) ranked ON ranked.id = gc.id
SET gc.display_order = ranked.display_order;

ALTER TABLE generated_courses
    MODIFY COLUMN display_order INT NOT NULL;
