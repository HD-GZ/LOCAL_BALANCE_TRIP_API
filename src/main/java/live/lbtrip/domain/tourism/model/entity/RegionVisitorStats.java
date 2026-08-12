package live.lbtrip.domain.tourism.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region_visitor_stats",
    uniqueConstraints = @UniqueConstraint(name = "uk_region_visitor_stats_candidate",
        columnNames = {"region_candidate_id", "base_date", "visitor_type"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionVisitorStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_candidate_id", nullable = false)
    private RegionCandidate regionCandidate;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visitor_type", nullable = false, length = 20)
    private VisitorType visitorType;

    @Column(name = "visitor_count", nullable = false)
    private double visitorCount;

    private RegionVisitorStats(
        RegionCandidate regionCandidate,
        LocalDate baseDate, VisitorType visitorType, double visitorCount
    ) {
        this.regionCandidate = regionCandidate;
        this.baseDate = baseDate;
        this.visitorType = visitorType;
        this.visitorCount = visitorCount;
    }

    public static RegionVisitorStats create(
        RegionCandidate regionCandidate,
        LocalDate baseDate, VisitorType visitorType, double visitorCount
    ) {
        return new RegionVisitorStats(regionCandidate, baseDate, visitorType, visitorCount);
    }

    public void updateCount(double visitorCount) {
        this.visitorCount = visitorCount;
    }
}
