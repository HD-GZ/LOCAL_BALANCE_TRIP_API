package live.lbtrip.domain.terms.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.TermsFixture;

class TermsTypeTest {

    @Nested
    class 변환 {

        @Test
        void 소문자_약관_종류를_변환한다() {
            assertThat(TermsType.from("service")).isEqualTo(TermsType.SERVICE);
            assertThat(TermsType.from("privacy")).isEqualTo(TermsType.PRIVACY);
            assertThat(TermsType.from("marketing")).isEqualTo(TermsType.MARKETING);
        }

        @Test
        void 대문자_약관_종류를_변환한다() {
            assertThat(TermsType.from("SERVICE")).isEqualTo(TermsType.SERVICE);
        }

        @Test
        void 앞뒤_공백을_제거하고_변환한다() {
            assertThat(TermsType.from("  privacy  ")).isEqualTo(TermsType.PRIVACY);
        }

        @Test
        void 지원하지_않는_약관_종류면_예외를_던진다() {
            assertThatThrownBy(() -> TermsType.from(TermsFixture.UNSUPPORTED_TYPE_PATH))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_FOUND);
        }
    }
}
