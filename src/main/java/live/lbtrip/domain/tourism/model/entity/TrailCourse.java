package live.lbtrip.domain.tourism.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.dto.DurunubiCourseItem;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "trail_courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrailCourse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crs_idx", nullable = false, unique = true, length = 50)
    private String crsIdx;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "brd_div", length = 20)
    private String brdDiv;

    @Column(name = "route_idx", length = 50)
    private String routeIdx;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "required_minutes")
    private Integer requiredMinutes;

    private Integer level;

    @Column(length = 100)
    private String sigun;

    @Column(name = "gpx_path", length = 500)
    private String gpxPath;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_candidate_id")
    private RegionCandidate regionCandidate;

    private TrailCourse(DurunubiCourseItem item, RegionCandidate regionCandidate) {
        this.crsIdx = item.crsIdx();
        apply(item);
        this.regionCandidate = regionCandidate;
    }

    public static TrailCourse create(DurunubiCourseItem item, RegionCandidate regionCandidate) {
        return new TrailCourse(item, regionCandidate);
    }

    public void update(DurunubiCourseItem item, RegionCandidate regionCandidate) {
        apply(item);
        assignRegion(regionCandidate);
    }

    public void assignRegion(RegionCandidate regionCandidate) {
        this.regionCandidate = regionCandidate;
    }

    private void apply(DurunubiCourseItem item) {
        this.name = item.crsKorNm();
        this.brdDiv = item.brdDiv();
        this.routeIdx = item.routeIdx();
        this.distanceKm = item.crsDstnc();
        this.requiredMinutes = item.crsTotlRqrdHour();
        this.level = item.crsLevel();
        this.sigun = item.sigun();
        this.gpxPath = item.gpxpath();
        this.summary = item.crsSummary();
    }
}
