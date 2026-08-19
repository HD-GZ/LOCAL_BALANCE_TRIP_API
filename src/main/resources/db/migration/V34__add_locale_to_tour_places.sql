ALTER TABLE tour_places
    ADD COLUMN locale VARCHAR(5) NOT NULL DEFAULT 'ko' AFTER id;

ALTER TABLE tour_places
    DROP INDEX uk_tour_places_content_id,
    ADD CONSTRAINT uk_tour_places_locale_content_id UNIQUE (locale, content_id);

ALTER TABLE tour_places
    DROP COLUMN eng_content_id,
    DROP COLUMN title_en,
    DROP COLUMN overview_en;
