package live.lbtrip.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthRequestFixture;
import live.lbtrip.support.fixture.UserFixture;

class EmailVerificationTokenTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Nested
    class 사용 {

        @Test
        void 인증_토큰을_사용한다() {
            EmailVerificationToken token = usableToken();

            token.use(NOW);

            assertThat(token.isUsed()).isTrue();
        }

        @Test
        void 이미_사용한_인증_토큰이면_예외를_던진다() {
            EmailVerificationToken token = usableToken();
            token.use(NOW);

            assertThatThrownBy(() -> token.use(NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_USED);
        }

        @Test
        void 만료된_인증_토큰이면_예외를_던진다() {
            EmailVerificationToken token = EmailVerificationToken.create(
                UserFixture.user(),
                AuthRequestFixture.VERIFICATION_CODE,
                NOW.minusSeconds(1)
            );

            assertThatThrownBy(() -> token.use(NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }
    }

    private EmailVerificationToken usableToken() {
        return EmailVerificationToken.create(
            UserFixture.user(),
            AuthRequestFixture.VERIFICATION_CODE,
            NOW.plusMinutes(5)
        );
    }
}
