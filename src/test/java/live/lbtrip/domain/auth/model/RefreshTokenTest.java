package live.lbtrip.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.TokenFixture;
import live.lbtrip.support.fixture.UserFixture;

class RefreshTokenTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Nested
    class 만료_검증 {

        @Test
        void 사용_가능한_토큰이면_예외를_던지지_않는다() {
            RefreshToken refreshToken = RefreshToken.create(
                UserFixture.user(),
                TokenFixture.REFRESH_TOKEN,
                NOW.plusSeconds(60)
            );

            assertThatCode(() -> refreshToken.validateNotExpired(NOW))
                .doesNotThrowAnyException();
        }

        @Test
        void 만료된_토큰이면_예외를_던진다() {
            RefreshToken refreshToken = RefreshToken.create(
                UserFixture.user(),
                TokenFixture.REFRESH_TOKEN,
                NOW.minusSeconds(1)
            );

            assertThatThrownBy(() -> refreshToken.validateNotExpired(NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
    }
}
