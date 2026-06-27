package live.lbtrip.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class EmailVerificationTokenTest {

    @Test
    void useMarksTokenUsed() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        EmailVerificationToken token = EmailVerificationToken.create(user(), "123456", now.plusMinutes(5));

        token.use(now);

        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void useRejectsUsedToken() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        EmailVerificationToken token = EmailVerificationToken.create(user(), "123456", now.plusMinutes(5));
        token.use(now);

        assertThatThrownBy(() -> token.use(now))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_USED);
    }

    @Test
    void useRejectsExpiredToken() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        EmailVerificationToken token = EmailVerificationToken.create(user(), "123456", now.minusSeconds(1));

        assertThatThrownBy(() -> token.use(now))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
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
