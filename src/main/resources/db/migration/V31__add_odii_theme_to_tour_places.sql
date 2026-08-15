ALTER TABLE tour_places
    ADD COLUMN odii_theme_id BIGINT NULL;

ALTER TABLE tour_places
    ADD CONSTRAINT fk_tour_places_odii_theme
        FOREIGN KEY (odii_theme_id) REFERENCES odii_themes (id);
