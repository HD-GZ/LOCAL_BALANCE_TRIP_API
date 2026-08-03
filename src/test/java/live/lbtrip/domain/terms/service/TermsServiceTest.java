package live.lbtrip.domain.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.terms.dto.response.TermsResponse;
import live.lbtrip.domain.terms.model.TermsType;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.TermsFixture;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @Mock
    private TermsFinder termsFinder;

    @InjectMocks
    private TermsService termsService;

    @Nested
    class 조회 {

        @Test
        void 약관_전문을_조회한다() {
            when(termsFinder.findEffective(any(TermsType.class), any(LocalDate.class)))
                .thenReturn(TermsFixture.terms());

            TermsResponse response = termsService.getTerms(TermsFixture.TYPE_PATH);

            assertThat(response.type()).isEqualTo(TermsFixture.TYPE);
            assertThat(response.title()).isEqualTo(TermsFixture.TITLE);
            assertThat(response.version()).isEqualTo(TermsFixture.VERSION);
            assertThat(response.effectiveDate()).isEqualTo(TermsFixture.EFFECTIVE_DATE);
            assertThat(response.content()).isEqualTo(TermsFixture.CONTENT);
        }

        @Test
        void 오늘_날짜를_기준으로_시행_중인_약관을_조회한다() {
            when(termsFinder.findEffective(TermsType.SERVICE, LocalDate.now()))
                .thenReturn(TermsFixture.terms());

            termsService.getTerms(TermsFixture.TYPE_PATH);

            verify(termsFinder).findEffective(TermsType.SERVICE, LocalDate.now());
        }

        @Test
        void 지원하지_않는_약관_종류면_조회하지_않고_예외를_던진다() {
            assertThatThrownBy(() -> termsService.getTerms(TermsFixture.UNSUPPORTED_TYPE_PATH))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_FOUND);

            verify(termsFinder, never()).findEffective(any(TermsType.class), any(LocalDate.class));
        }
    }
}
