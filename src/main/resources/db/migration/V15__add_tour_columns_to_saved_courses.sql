ALTER TABLE saved_courses
    ADD COLUMN tour_started_at TIMESTAMP NULL,
    ADD COLUMN tour_ended_at TIMESTAMP NULL;

ALTER TABLE saved_course_places
    ADD COLUMN visited_at TIMESTAMP NULL;
