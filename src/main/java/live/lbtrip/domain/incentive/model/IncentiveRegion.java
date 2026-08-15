package live.lbtrip.domain.incentive.model;

import java.util.Objects;

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
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "incentive_regions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_incentive_regions_candidate",
        columnNames = {"incentive_id", "region_candidate_id"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncentiveRegion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incentive_id", nullable = false)
    private Incentive incentive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_candidate_id", nullable = false)
    private RegionCandidate regionCandidate;

    private IncentiveRegion(RegionCandidate regionCandidate) {
        this.regionCandidate = regionCandidate;
    }

    public static IncentiveRegion create(RegionCandidate regionCandidate) {
        return new IncentiveRegion(regionCandidate);
    }

    void assignIncentive(Incentive incentive) {
        this.incentive = incentive;
    }

    boolean hasSameRegion(IncentiveRegion other) {
        return Objects.equals(regionCandidate.getId(), other.regionCandidate.getId());
    }
}
