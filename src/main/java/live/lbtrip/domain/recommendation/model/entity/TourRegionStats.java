package live.lbtrip.domain.recommendation.model.entity;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.domain.recommendation.model.enums.TourContentType;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tour_region_stats",
    uniqueConstraints = @UniqueConstraint(name = "uk_tour_region_stats",
        columnNames = {"ldong_regn_cd", "ldong_signgu_cd"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourRegionStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ldong_regn_cd", nullable = false, length = 2)
    private String ldongRegnCd;

    @Column(name = "ldong_signgu_cd", nullable = false, length = 3)
    private String ldongSignguCd;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize;

    @Column(name = "tourist_spot_count", nullable = false)
    private int touristSpotCount;

    @Column(name = "cultural_facility_count", nullable = false)
    private int culturalFacilityCount;

    @Column(name = "leports_count", nullable = false)
    private int leportsCount;

    @Column(name = "accommodation_count", nullable = false)
    private int accommodationCount;

    @Column(name = "shopping_count", nullable = false)
    private int shoppingCount;

    @Column(name = "restaurant_count", nullable = false)
    private int restaurantCount;

    private TourRegionStats(
        String ldongRegnCd, String ldongSignguCd,
        int totalCount, int sampleSize, Map<Integer, Integer> typeCounts
    ) {
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
        apply(totalCount, sampleSize, typeCounts);
    }

    public static TourRegionStats create(
        String ldongRegnCd, String ldongSignguCd,
        int totalCount, int sampleSize, Map<Integer, Integer> typeCounts
    ) {
        return new TourRegionStats(ldongRegnCd, ldongSignguCd, totalCount, sampleSize, typeCounts);
    }

    public void update(int totalCount, int sampleSize, Map<Integer, Integer> typeCounts) {
        apply(totalCount, sampleSize, typeCounts);
    }

    public Map<Integer, Integer> toTypeCounts() {
        return Map.of(
            TourContentType.TOURIST_SPOT.getCode(), touristSpotCount,
            TourContentType.CULTURAL_FACILITY.getCode(), culturalFacilityCount,
            TourContentType.LEPORTS.getCode(), leportsCount,
            TourContentType.ACCOMMODATION.getCode(), accommodationCount,
            TourContentType.SHOPPING.getCode(), shoppingCount,
            TourContentType.RESTAURANT.getCode(), restaurantCount);
    }

    private void apply(int totalCount, int sampleSize, Map<Integer, Integer> typeCounts) {
        this.totalCount = totalCount;
        this.sampleSize = sampleSize;
        this.touristSpotCount = countOf(typeCounts, TourContentType.TOURIST_SPOT);
        this.culturalFacilityCount = countOf(typeCounts, TourContentType.CULTURAL_FACILITY);
        this.leportsCount = countOf(typeCounts, TourContentType.LEPORTS);
        this.accommodationCount = countOf(typeCounts, TourContentType.ACCOMMODATION);
        this.shoppingCount = countOf(typeCounts, TourContentType.SHOPPING);
        this.restaurantCount = countOf(typeCounts, TourContentType.RESTAURANT);
    }

    private int countOf(Map<Integer, Integer> typeCounts, TourContentType type) {
        return typeCounts.getOrDefault(type.getCode(), 0);
    }
}
