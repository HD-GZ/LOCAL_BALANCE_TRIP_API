ALTER TABLE terms
    ADD COLUMN title_en VARCHAR(100) NULL AFTER title,
    ADD COLUMN content_en TEXT NULL AFTER content;
