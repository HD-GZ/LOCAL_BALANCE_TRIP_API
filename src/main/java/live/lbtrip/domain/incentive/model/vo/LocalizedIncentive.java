package live.lbtrip.domain.incentive.model.vo;

import java.time.LocalDate;
import java.util.Locale;

import live.lbtrip.domain.incentive.model.Incentive;

public record LocalizedIncentive(
    String title,
    String description,
    String url,
    LocalDate endDate
) {

    public static LocalizedIncentive of(Incentive incentive, Locale locale) {
        return new LocalizedIncentive(
            incentive.titleFor(locale),
            incentive.descriptionFor(locale),
            incentive.getUrl(),
            incentive.getEndDate());
    }
}
