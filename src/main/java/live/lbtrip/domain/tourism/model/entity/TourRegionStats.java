package live.lbtrip.domain.tourism.model.entity;

import java.util.EnumMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tour_region_stats",
    uniqueConstraints = @UniqueConstraint(name = "uk_tour_region_stats_candidate",
        columnNames = "region_candidate_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourRegionStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_candidate_id", nullable = false)
    private RegionCandidate regionCandidate;

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

    @Column(name = "luxury_shopping_count", nullable = false)
    private int luxuryShoppingCount;

    @Column(name = "traditional_market_count", nullable = false)
    private int traditionalMarketCount;

    @Column(name = "viewing_place_count", nullable = false)
    private int viewingPlaceCount;

    @Column(name = "experience_place_count", nullable = false)
    private int experiencePlaceCount;

    @Column(name = "nature_rest_count", nullable = false)
    private int natureRestCount;

    @Column(name = "cafe_count", nullable = false)
    private int cafeCount;

    @Column(name = "exhibition_count", nullable = false)
    private int exhibitionCount;

    private TourRegionStats(
        RegionCandidate regionCandidate,
        int totalCount, int sampleSize,
        Map<Integer, Integer> typeCounts, Map<CategoryGroup, Integer> groupCounts
    ) {
        this.regionCandidate = regionCandidate;
        apply(totalCount, sampleSize, typeCounts, groupCounts);
    }

    public static TourRegionStats create(
        RegionCandidate regionCandidate,
        int totalCount, int sampleSize,
        Map<Integer, Integer> typeCounts, Map<CategoryGroup, Integer> groupCounts
    ) {
        return new TourRegionStats(regionCandidate, totalCount, sampleSize, typeCounts, groupCounts);
    }

    public void update(
        int totalCount, int sampleSize,
        Map<Integer, Integer> typeCounts, Map<CategoryGroup, Integer> groupCounts
    ) {
        apply(totalCount, sampleSize, typeCounts, groupCounts);
    }

    public Map<Integer, Integer> toTypeCounts() {
        return Map.of(
            TourContentType.TOURIST_SPOT.getCode(), touristSpotCount,
            TourContentType.CULTURAL_FACILITY.getCode(), culturalFacilityCount,
            TourContentType.LEPORTS.getCode(), leportsCount,
            TourContentType.ACCOMMODATION.getCode(), accommodationCount,
            TourContentType.SHOPPING.getCode(), shoppingCount,
            TourContentType.RESTAURANT.getCode(), restaurantCount
        );
    }

    public Map<CategoryGroup, Integer> toGroupCounts() {
        Map<CategoryGroup, Integer> counts = new EnumMap<>(CategoryGroup.class);
        counts.put(CategoryGroup.LUXURY_SHOPPING, luxuryShoppingCount);
        counts.put(CategoryGroup.TRADITIONAL_MARKET, traditionalMarketCount);
        counts.put(CategoryGroup.VIEWING_PLACE, viewingPlaceCount);
        counts.put(CategoryGroup.EXPERIENCE_PLACE, experiencePlaceCount);
        counts.put(CategoryGroup.NATURE_REST, natureRestCount);
        counts.put(CategoryGroup.CAFE, cafeCount);
        counts.put(CategoryGroup.EXHIBITION, exhibitionCount);
        return counts;
    }

    private void apply(
        int totalCount, int sampleSize,
        Map<Integer, Integer> typeCounts, Map<CategoryGroup, Integer> groupCounts
    ) {
        this.totalCount = totalCount;
        this.sampleSize = sampleSize;
        this.touristSpotCount = countOf(typeCounts, TourContentType.TOURIST_SPOT);
        this.culturalFacilityCount = countOf(typeCounts, TourContentType.CULTURAL_FACILITY);
        this.leportsCount = countOf(typeCounts, TourContentType.LEPORTS);
        this.accommodationCount = countOf(typeCounts, TourContentType.ACCOMMODATION);
        this.shoppingCount = countOf(typeCounts, TourContentType.SHOPPING);
        this.restaurantCount = countOf(typeCounts, TourContentType.RESTAURANT);
        this.luxuryShoppingCount = groupCounts.getOrDefault(CategoryGroup.LUXURY_SHOPPING, 0);
        this.traditionalMarketCount = groupCounts.getOrDefault(CategoryGroup.TRADITIONAL_MARKET, 0);
        this.viewingPlaceCount = groupCounts.getOrDefault(CategoryGroup.VIEWING_PLACE, 0);
        this.experiencePlaceCount = groupCounts.getOrDefault(CategoryGroup.EXPERIENCE_PLACE, 0);
        this.natureRestCount = groupCounts.getOrDefault(CategoryGroup.NATURE_REST, 0);
        this.cafeCount = groupCounts.getOrDefault(CategoryGroup.CAFE, 0);
        this.exhibitionCount = groupCounts.getOrDefault(CategoryGroup.EXHIBITION, 0);
    }

    private int countOf(Map<Integer, Integer> typeCounts, TourContentType type) {
        return typeCounts.getOrDefault(type.getCode(), 0);
    }
}
