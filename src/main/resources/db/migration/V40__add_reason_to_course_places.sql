ALTER TABLE course_places
    ADD COLUMN reason VARCHAR(150) NULL;

ALTER TABLE saved_course_places
    ADD COLUMN reason VARCHAR(150) NULL;
