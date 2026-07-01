ALTER TABLE propensities DROP COLUMN flexibility;

ALTER TABLE propensities ADD COLUMN accommodation INT NULL;
ALTER TABLE propensities ADD COLUMN food INT NULL;
ALTER TABLE propensities ADD COLUMN experience INT NULL;
ALTER TABLE propensities ADD COLUMN transportation INT NULL;
ALTER TABLE propensities ADD COLUMN cafe_exhibition INT NULL;

UPDATE propensities
SET accommodation = 3,
    food = 3,
    experience = 3,
    transportation = 3,
    cafe_exhibition = 3
WHERE accommodation IS NULL
   OR food IS NULL
   OR experience IS NULL
   OR transportation IS NULL
   OR cafe_exhibition IS NULL;

ALTER TABLE propensities MODIFY COLUMN accommodation INT NOT NULL;
ALTER TABLE propensities MODIFY COLUMN food INT NOT NULL;
ALTER TABLE propensities MODIFY COLUMN experience INT NOT NULL;
ALTER TABLE propensities MODIFY COLUMN transportation INT NOT NULL;
ALTER TABLE propensities MODIFY COLUMN cafe_exhibition INT NOT NULL;
