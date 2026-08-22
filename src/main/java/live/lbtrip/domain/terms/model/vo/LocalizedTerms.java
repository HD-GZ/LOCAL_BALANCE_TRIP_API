package live.lbtrip.domain.terms.model.vo;

import java.time.LocalDate;
import java.util.Locale;

import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.model.TermsType;

public record LocalizedTerms(
    TermsType type,
    String title,
    String version,
    LocalDate effectiveDate,
    String content
) {

    public static LocalizedTerms of(Terms terms, Locale locale) {
        return new LocalizedTerms(
            terms.getType(),
            terms.titleFor(locale),
            terms.getVersion(),
            terms.getEffectiveDate(),
            terms.contentFor(locale));
    }
}
