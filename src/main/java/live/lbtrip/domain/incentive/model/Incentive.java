package live.lbtrip.domain.incentive.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "incentives")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incentive extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "행사 제목은 필수입니다.")
    @Size(max = 200, message = "행사 제목은 200자 이하여야 합니다.")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "행사 페이지 URL은 필수입니다.")
    @Size(max = 500, message = "행사 페이지 URL은 500자 이하여야 합니다.")
    @Column(nullable = false, length = 500)
    private String url;

    @Size(max = 200, message = "행사 부가 설명은 200자 이하여야 합니다.")
    @Column(length = 200)
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "incentive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncentiveRegion> regions = new ArrayList<>();

    private Incentive(String title, String url, String description, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Incentive create(String title, String url, String description, LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return new Incentive(title, url, description, startDate, endDate);
    }

    public void update(String title, String url, String description, LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        this.title = title;
        this.url = url;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || (endDate != null && endDate.isBefore(startDate))) {
            throw BusinessException.of(ErrorCode.INVALID_INCENTIVE_PERIOD);
        }
    }

    public void replaceRegions(List<IncentiveRegion> newRegions) {
        regions.removeIf(existing -> newRegions.stream().noneMatch(existing::hasSameCode));
        for (IncentiveRegion region : newRegions) {
            if (regions.stream().noneMatch(region::hasSameCode)) {
                regions.add(region);
                region.assignIncentive(this);
            }
        }
    }
}
