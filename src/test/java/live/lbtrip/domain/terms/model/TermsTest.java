package live.lbtrip.domain.terms.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.TermsFixture;

class TermsTest {

    @Nested
    class 로케일별_텍스트_조회 {

        @Test
        void 영어_로케일이면_영문_제목과_전문을_반환한다() {
            Terms terms = TermsFixture.termsWithEnglish();

            assertThat(terms.titleFor(Locale.ENGLISH)).isEqualTo(TermsFixture.TITLE_EN);
            assertThat(terms.contentFor(Locale.ENGLISH)).isEqualTo(TermsFixture.CONTENT_EN);
        }

        @Test
        void 한국어_로케일이면_한글_제목과_전문을_반환한다() {
            Terms terms = TermsFixture.termsWithEnglish();

            assertThat(terms.titleFor(Locale.KOREAN)).isEqualTo(TermsFixture.TITLE);
            assertThat(terms.contentFor(Locale.KOREAN)).isEqualTo(TermsFixture.CONTENT);
        }

        @Test
        void 영문_번역이_없으면_한글로_폴백한다() {
            Terms terms = TermsFixture.terms();

            assertThat(terms.titleFor(Locale.ENGLISH)).isEqualTo(TermsFixture.TITLE);
            assertThat(terms.contentFor(Locale.ENGLISH)).isEqualTo(TermsFixture.CONTENT);
        }
    }
}
