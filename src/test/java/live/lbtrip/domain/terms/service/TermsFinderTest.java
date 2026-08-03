package live.lbtrip.domain.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.repository.TermsRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.TermsFixture;

@ExtendWith(MockitoExtension.class)
class TermsFinderTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 8, 3);

    @Mock
    private TermsRepository termsRepository;

    @InjectMocks
    private TermsFinder termsFinder;

    @Nested
    class 조회 {

        @Test
        void 시행_중인_약관을_조회한다() {
            when(termsRepository.findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                TermsFixture.TYPE,
                BASE_DATE
            )).thenReturn(Optional.of(TermsFixture.terms()));

            Terms terms = termsFinder.findEffective(TermsFixture.TYPE, BASE_DATE);

            assertThat(terms.getType()).isEqualTo(TermsFixture.TYPE);
            assertThat(terms.getVersion()).isEqualTo(TermsFixture.VERSION);
            assertThat(terms.getEffectiveDate()).isEqualTo(TermsFixture.EFFECTIVE_DATE);
        }

        @Test
        void 시행_중인_약관이_없으면_예외를_던진다() {
            when(termsRepository.findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                TermsFixture.TYPE,
                BASE_DATE
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() -> termsFinder.findEffective(TermsFixture.TYPE, BASE_DATE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_FOUND);
        }
    }
}
