ALTER TABLE tour_places ADD COLUMN region_candidate_id BIGINT NULL;
UPDATE tour_places tp
JOIN region_candidates rc
    ON rc.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci = tp.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci
        AND rc.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci = tp.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci
SET tp.region_candidate_id = rc.id;
ALTER TABLE tour_places MODIFY COLUMN region_candidate_id BIGINT NOT NULL;
ALTER TABLE tour_places
    ADD CONSTRAINT fk_tour_places_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id);
ALTER TABLE tour_places DROP INDEX idx_tour_places_region;
ALTER TABLE tour_places DROP COLUMN ldong_regn_cd, DROP COLUMN ldong_signgu_cd;

ALTER TABLE tour_region_stats ADD COLUMN region_candidate_id BIGINT NULL;
UPDATE tour_region_stats trs
JOIN region_candidates rc
    ON rc.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci = trs.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci
        AND rc.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci = trs.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci
SET trs.region_candidate_id = rc.id;
ALTER TABLE tour_region_stats MODIFY COLUMN region_candidate_id BIGINT NOT NULL;
ALTER TABLE tour_region_stats
    ADD CONSTRAINT uk_tour_region_stats_candidate UNIQUE (region_candidate_id);
ALTER TABLE tour_region_stats
    ADD CONSTRAINT fk_tour_region_stats_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id);
ALTER TABLE tour_region_stats DROP INDEX uk_tour_region_stats;
ALTER TABLE tour_region_stats DROP COLUMN ldong_regn_cd, DROP COLUMN ldong_signgu_cd;

ALTER TABLE region_visitor_stats ADD COLUMN region_candidate_id BIGINT NULL;
UPDATE region_visitor_stats rvs
JOIN region_candidates rc
    ON rc.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci = rvs.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci
        AND rc.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci = rvs.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci
SET rvs.region_candidate_id = rc.id;
ALTER TABLE region_visitor_stats MODIFY COLUMN region_candidate_id BIGINT NOT NULL;
ALTER TABLE region_visitor_stats
    ADD CONSTRAINT uk_region_visitor_stats_candidate
        UNIQUE (region_candidate_id, base_date, visitor_type);
ALTER TABLE region_visitor_stats
    ADD CONSTRAINT fk_region_visitor_stats_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id);
ALTER TABLE region_visitor_stats DROP INDEX uk_region_visitor_stats;
ALTER TABLE region_visitor_stats DROP COLUMN ldong_regn_cd, DROP COLUMN ldong_signgu_cd;

ALTER TABLE incentive_regions ADD COLUMN region_candidate_id BIGINT NULL;
UPDATE incentive_regions ir
JOIN region_candidates rc
    ON rc.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci = ir.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci
        AND rc.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci = ir.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci
SET ir.region_candidate_id = rc.id;
ALTER TABLE incentive_regions MODIFY COLUMN region_candidate_id BIGINT NOT NULL;
ALTER TABLE incentive_regions
    ADD CONSTRAINT uk_incentive_regions_candidate UNIQUE (incentive_id, region_candidate_id);
ALTER TABLE incentive_regions
    ADD CONSTRAINT fk_incentive_regions_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id);
ALTER TABLE incentive_regions DROP INDEX uk_incentive_regions;
ALTER TABLE incentive_regions DROP COLUMN ldong_regn_cd, DROP COLUMN ldong_signgu_cd;

ALTER TABLE recommended_regions ADD COLUMN region_candidate_id BIGINT NULL;
UPDATE recommended_regions rr
JOIN region_candidates rc
    ON rc.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci = rr.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci
        AND rc.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci = rr.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci
SET rr.region_candidate_id = rc.id;
DELETE FROM course_places
WHERE course_id IN (
    SELECT id FROM generated_courses
    WHERE recommended_region_id IN (
        SELECT id FROM recommended_regions WHERE region_candidate_id IS NULL));
DELETE FROM generated_courses
WHERE recommended_region_id IN (
    SELECT id FROM recommended_regions WHERE region_candidate_id IS NULL);
DELETE FROM recommended_regions WHERE region_candidate_id IS NULL;
ALTER TABLE recommended_regions MODIFY COLUMN region_candidate_id BIGINT NOT NULL;
ALTER TABLE recommended_regions
    ADD CONSTRAINT fk_recommended_regions_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id);
ALTER TABLE recommended_regions DROP COLUMN ldong_regn_cd, DROP COLUMN ldong_signgu_cd;

ALTER TABLE saved_courses ADD COLUMN region_candidate_id BIGINT NULL;
UPDATE saved_courses sc
JOIN region_candidates rc
    ON rc.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci = sc.ldong_regn_cd COLLATE utf8mb4_0900_ai_ci
        AND rc.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci = sc.ldong_signgu_cd COLLATE utf8mb4_0900_ai_ci
SET sc.region_candidate_id = rc.id;
ALTER TABLE saved_courses
    ADD CONSTRAINT fk_saved_courses_region_candidate
        FOREIGN KEY (region_candidate_id) REFERENCES region_candidates (id)
        ON DELETE SET NULL;
