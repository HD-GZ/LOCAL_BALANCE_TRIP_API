package live.lbtrip.domain.auth.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class SignupRequestTest {

    @Test
    void createSignupRequest() {
        SignupRequest request = new SignupRequest(
            "홍길동",
            "user@example.com",
            "password1",
            "password1",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            true,
            true,
            false
        );

        assertThat(request.password()).isEqualTo("password1");
        assertThat(request.passwordConfirm()).isEqualTo("password1");
    }

    @Test
    void createRejectsPasswordConfirmMismatch() {
        assertThatThrownBy(() -> new SignupRequest(
            "홍길동",
            "user@example.com",
            "password1",
            "password2",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            true,
            true,
            false
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
    }
}
