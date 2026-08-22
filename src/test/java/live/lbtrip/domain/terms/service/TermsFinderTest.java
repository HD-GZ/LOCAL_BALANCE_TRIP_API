package live.lbtrip.domain.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.model.vo.LocalizedTerms;
import live.lbtrip.domain.terms.repository.TermsRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.MessageResolver;
import live.lbtrip.support.fixture.TermsFixture;

@ExtendWith(MockitoExtension.class)
class TermsFinderTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 8, 3);

    @Mock
    private TermsRepository termsRepository;

    @Mock
    private MessageResolver messageResolver;

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

    @Nested
    class 로케일별_조회 {

        @Test
        void 한국어_로케일이면_한글_제목과_전문으로_조회한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.KOREAN);
            when(termsRepository.findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                TermsFixture.TYPE,
                BASE_DATE
            )).thenReturn(Optional.of(TermsFixture.termsWithEnglish()));

            LocalizedTerms terms = termsFinder.findEffectiveLocalized(TermsFixture.TYPE, BASE_DATE);

            assertThat(terms.title()).isEqualTo(TermsFixture.TITLE);
            assertThat(terms.content()).isEqualTo(TermsFixture.CONTENT);
        }

        @Test
        void 영어_로케일이면_영문_제목과_전문으로_조회한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(termsRepository.findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                TermsFixture.TYPE,
                BASE_DATE
            )).thenReturn(Optional.of(TermsFixture.termsWithEnglish()));

            LocalizedTerms terms = termsFinder.findEffectiveLocalized(TermsFixture.TYPE, BASE_DATE);

            assertThat(terms.title()).isEqualTo(TermsFixture.TITLE_EN);
            assertThat(terms.content()).isEqualTo(TermsFixture.CONTENT_EN);
        }

        @Test
        void 영어_로케일이어도_영문_번역이_없으면_한글로_폴백한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(termsRepository.findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                TermsFixture.TYPE,
                BASE_DATE
            )).thenReturn(Optional.of(TermsFixture.terms()));

            LocalizedTerms terms = termsFinder.findEffectiveLocalized(TermsFixture.TYPE, BASE_DATE);

            assertThat(terms.title()).isEqualTo(TermsFixture.TITLE);
            assertThat(terms.content()).isEqualTo(TermsFixture.CONTENT);
        }
    }
}
