package live.lbtrip.domain.user.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class UserTest {

    @Test
    void createUser() {
        User user = User.create(
            "홍길동",
            "user@example.com",
            "encoded-password",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            true,
            true,
            false
        );

        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1999, 1, 1));
        assertThat(user.getGender()).isEqualTo(Gender.NOT_SPECIFIED);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_EMAIL_VERIFICATION);
        assertThat(user.isActive()).isFalse();
        assertThat(user.isTermsAgreed()).isTrue();
        assertThat(user.isPrivacyAgreed()).isTrue();
        assertThat(user.isMarketingAgreed()).isFalse();
    }

    @Test
    void createRejectsMissingTermsAgreement() {
        assertThatThrownBy(() -> User.create(
            "홍길동",
            "user@example.com",
            "encoded-password",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            false,
            true,
            false
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
    }

    @Test
    void createRejectsMissingPrivacyAgreement() {
        assertThatThrownBy(() -> User.create(
            "홍길동",
            "user@example.com",
            "encoded-password",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            true,
            false,
            false
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
    }

    @Test
    void verifyEmailActivatesUser() {
        User user = User.create(
            "홍길동",
            "user@example.com",
            "encoded-password",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            true,
            true,
            false
        );

        user.verifyEmail();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
