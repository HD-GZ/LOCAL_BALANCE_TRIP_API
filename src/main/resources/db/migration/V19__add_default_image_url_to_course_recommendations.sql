ALTER TABLE recommended_regions
    MODIFY COLUMN image_url VARCHAR(500) NULL
        DEFAULT 'https://stage.images.lb-trip.live/logs/logo-mark-512+1.png';

ALTER TABLE generated_courses
    MODIFY COLUMN image_url VARCHAR(500) NULL
        DEFAULT 'https://stage.images.lb-trip.live/logs/logo-mark-512+1.png';

ALTER TABLE course_places
    MODIFY COLUMN image_url VARCHAR(500) NULL
        DEFAULT 'https://stage.images.lb-trip.live/logs/logo-mark-512+1.png';
