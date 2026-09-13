package live.lbtrip.domain.terms.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.model.TermsType;
import live.lbtrip.domain.terms.model.vo.LocalizedTerms;
import live.lbtrip.domain.terms.repository.TermsRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TermsFinder {

    private final TermsRepository termsRepository;
    private final MessageResolver messageResolver;

    public Terms findEffective(TermsType type, LocalDate baseDate) {
        return termsRepository
            .findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(type, baseDate)
            .orElseThrow(() -> BusinessException.of(ErrorCode.TERMS_NOT_FOUND));
    }

    public LocalizedTerms findEffectiveLocalized(TermsType type, LocalDate baseDate) {
        return LocalizedTerms.of(findEffective(type, baseDate), messageResolver.currentLocale());
    }
}
