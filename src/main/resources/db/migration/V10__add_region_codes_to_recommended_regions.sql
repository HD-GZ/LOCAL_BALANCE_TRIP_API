ALTER TABLE recommended_regions
    ADD COLUMN ldong_regn_cd VARCHAR(2) NULL,
    ADD COLUMN ldong_signgu_cd VARCHAR(3) NULL;

UPDATE recommended_regions rr
JOIN region_candidates rc ON rc.name = rr.region_name
SET rr.ldong_regn_cd = rc.ldong_regn_cd,
    rr.ldong_signgu_cd = rc.ldong_signgu_cd;
