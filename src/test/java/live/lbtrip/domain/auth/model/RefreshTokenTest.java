package live.lbtrip.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class RefreshTokenTest {

    @Test
    void validateNotExpiredPassesUsableToken() {
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        RefreshToken refreshToken = RefreshToken.create(user(), "refresh-token", now.plusSeconds(60));

        assertThatCode(() -> refreshToken.validateNotExpired(now))
            .doesNotThrowAnyException();
    }

    @Test
    void validateNotExpiredRejectsExpiredToken() {
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        RefreshToken refreshToken = RefreshToken.create(user(), "refresh-token", now.minusSeconds(1));

        assertThatThrownBy(() -> refreshToken.validateNotExpired(now))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    private User user() {
        return User.create(
            "홍길동",
            "user@example.com",
            "encoded-password",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            true,
            true,
            false
        );
    }
}
