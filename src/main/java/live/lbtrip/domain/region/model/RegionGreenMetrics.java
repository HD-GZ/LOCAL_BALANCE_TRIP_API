package live.lbtrip.domain.region.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region_green_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionGreenMetrics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_candidate_id", nullable = false, unique = true)
    private RegionCandidate regionCandidate;

    @Column(name = "gpx_adjacent", nullable = false)
    private boolean gpxAdjacent;

    @Column(name = "transit_accessible", nullable = false)
    private boolean transitAccessible;

    @Column(name = "local_commerce", nullable = false)
    private boolean localCommerce;

    @Column(name = "tourism_card_merchant", nullable = false)
    private boolean tourismCardMerchant;

    @Column(name = "population_decline", nullable = false)
    private boolean populationDecline;

    private RegionGreenMetrics(
        RegionCandidate regionCandidate, boolean gpxAdjacent, boolean transitAccessible,
        boolean localCommerce, boolean tourismCardMerchant, boolean populationDecline
    ) {
        this.regionCandidate = regionCandidate;
        this.gpxAdjacent = gpxAdjacent;
        this.transitAccessible = transitAccessible;
        this.localCommerce = localCommerce;
        this.tourismCardMerchant = tourismCardMerchant;
        this.populationDecline = populationDecline;
    }

    public static RegionGreenMetrics create(
        RegionCandidate regionCandidate, boolean gpxAdjacent, boolean transitAccessible,
        boolean localCommerce, boolean tourismCardMerchant, boolean populationDecline
    ) {
        return new RegionGreenMetrics(regionCandidate, gpxAdjacent, transitAccessible,
            localCommerce, tourismCardMerchant, populationDecline);
    }

    public void updateGpxAdjacent(boolean gpxAdjacent) {
        this.gpxAdjacent = gpxAdjacent;
    }

    public int greenScore() {
        int score = 0;
        for (boolean signal : new boolean[] {
            gpxAdjacent, transitAccessible, localCommerce, tourismCardMerchant, populationDecline
        }) {
            if (signal) {
                score++;
            }
        }
        return score;
    }
}
