package live.lbtrip.domain.tourism.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region_visitor_stats",
    uniqueConstraints = @UniqueConstraint(name = "uk_region_visitor_stats",
        columnNames = {"ldong_regn_cd", "ldong_signgu_cd", "base_date", "visitor_type"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionVisitorStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ldong_regn_cd", nullable = false, length = 2)
    private String ldongRegnCd;

    @Column(name = "ldong_signgu_cd", nullable = false, length = 3)
    private String ldongSignguCd;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visitor_type", nullable = false, length = 20)
    private VisitorType visitorType;

    @Column(name = "visitor_count", nullable = false)
    private double visitorCount;

    private RegionVisitorStats(
        String ldongRegnCd, String ldongSignguCd,
        LocalDate baseDate, VisitorType visitorType, double visitorCount
    ) {
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
        this.baseDate = baseDate;
        this.visitorType = visitorType;
        this.visitorCount = visitorCount;
    }

    public static RegionVisitorStats create(
        String ldongRegnCd, String ldongSignguCd,
        LocalDate baseDate, VisitorType visitorType, double visitorCount
    ) {
        return new RegionVisitorStats(ldongRegnCd, ldongSignguCd, baseDate, visitorType, visitorCount);
    }

    public void updateCount(double visitorCount) {
        this.visitorCount = visitorCount;
    }
}
