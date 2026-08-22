ALTER TABLE travel_profiles
    ADD COLUMN nickname_en VARCHAR(100) NULL AFTER nickname,
    ADD COLUMN description_en VARCHAR(1000) NULL AFTER description;
