package live.lbtrip.support.fixture;

import live.lbtrip.domain.auth.dto.response.EmailVerificationResponse;
import live.lbtrip.domain.auth.dto.response.LoginResponse;
import live.lbtrip.domain.auth.dto.response.SignupResponse;
import live.lbtrip.domain.auth.dto.response.TokenResponse;
import live.lbtrip.domain.user.model.UserStatus;

public final class AuthResponseFixture {

    public static final long USER_ID = 1L;
    public static final long VERIFICATION_CODE_EXPIRES_IN = 86_400L;

    private AuthResponseFixture() {
    }

    public static SignupResponse signupResponse() {
        return new SignupResponse(
            USER_ID,
            UserFixture.EMAIL,
            UserStatus.PENDING_EMAIL_VERIFICATION,
            VERIFICATION_CODE_EXPIRES_IN
        );
    }

    public static LoginResponse loginResponse() {
        return LoginResponse.of(TokenFixture.ACCESS_TOKEN, TokenFixture.REFRESH_TOKEN);
    }

    public static TokenResponse tokenResponse() {
        return TokenResponse.of(TokenFixture.NEW_ACCESS_TOKEN, TokenFixture.REFRESH_TOKEN);
    }

    public static EmailVerificationResponse emailVerificationResponse() {
        return new EmailVerificationResponse(USER_ID, UserFixture.EMAIL, UserStatus.ACTIVE);
    }
}
