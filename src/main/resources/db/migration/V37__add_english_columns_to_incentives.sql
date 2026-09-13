ALTER TABLE incentives
    ADD COLUMN title_en VARCHAR(200) NULL AFTER title,
    ADD COLUMN description_en VARCHAR(200) NULL AFTER description;
