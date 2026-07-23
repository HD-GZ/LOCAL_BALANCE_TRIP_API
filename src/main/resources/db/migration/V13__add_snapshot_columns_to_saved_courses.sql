ALTER TABLE saved_courses
    ADD COLUMN image_url VARCHAR(500) NULL,
    ADD COLUMN ldong_regn_cd VARCHAR(2) NULL,
    ADD COLUMN ldong_signgu_cd VARCHAR(3) NULL;

UPDATE saved_courses sc
JOIN generated_courses gc ON gc.id = sc.source_course_id
JOIN recommended_regions rr ON rr.id = gc.recommended_region_id
SET sc.image_url = gc.image_url,
    sc.ldong_regn_cd = rr.ldong_regn_cd,
    sc.ldong_signgu_cd = rr.ldong_signgu_cd;
