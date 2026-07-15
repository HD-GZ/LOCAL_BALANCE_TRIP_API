package live.lbtrip.domain.admin.incentive.model;

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
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "incentive_regions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_incentive_regions",
        columnNames = {"incentive_id", "ldong_regn_cd", "ldong_signgu_cd"}
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

    @Column(name = "ldong_regn_cd", nullable = false, length = 2)
    private String ldongRegnCd;

    @Column(name = "ldong_signgu_cd", nullable = false, length = 3)
    private String ldongSignguCd;

    private IncentiveRegion(String ldongRegnCd, String ldongSignguCd) {
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
    }

    public static IncentiveRegion create(String ldongRegnCd, String ldongSignguCd) {
        return new IncentiveRegion(ldongRegnCd, ldongSignguCd);
    }

    void assignIncentive(Incentive incentive) {
        this.incentive = incentive;
    }

    boolean hasSameCode(IncentiveRegion other) {
        return ldongRegnCd.equals(other.ldongRegnCd) && ldongSignguCd.equals(other.ldongSignguCd);
    }
}
