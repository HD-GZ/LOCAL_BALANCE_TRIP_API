package live.lbtrip.domain.terms.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.terms.dto.response.TermsResponse;
import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.model.TermsType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsFinder termsFinder;

    public TermsResponse getTerms(String type) {
        Terms terms = termsFinder.findEffective(TermsType.from(type), LocalDate.now());
        return TermsResponse.from(terms);
    }
}
