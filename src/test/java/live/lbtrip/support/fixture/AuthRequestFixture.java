package live.lbtrip.support.fixture;

import live.lbtrip.domain.auth.dto.request.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.request.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.request.LoginRequest;
import live.lbtrip.domain.auth.dto.request.SignupRequest;
import live.lbtrip.domain.auth.dto.request.TokenRefreshRequest;

public final class AuthRequestFixture {

    public static final String VERIFICATION_CODE = "123456";

    private AuthRequestFixture() {
    }

    public static SignupRequest signupRequest() {
        return signupRequest(UserFixture.EMAIL);
    }

    public static SignupRequest signupRequest(String email) {
        return new SignupRequest(
            UserFixture.NAME,
            email,
            UserFixture.PASSWORD,
            UserFixture.PASSWORD,
            UserFixture.BIRTH_DATE,
            UserFixture.GENDER,
            true,
            true,
            false
        );
    }

    public static LoginRequest loginRequest() {
        return new LoginRequest(UserFixture.EMAIL, UserFixture.PASSWORD);
    }

    public static TokenRefreshRequest tokenRefreshRequest() {
        return new TokenRefreshRequest(TokenFixture.REFRESH_TOKEN);
    }

    public static EmailVerificationConfirmRequest emailVerificationConfirmRequest() {
        return new EmailVerificationConfirmRequest(VERIFICATION_CODE);
    }

    public static EmailVerificationResendRequest emailVerificationResendRequest() {
        return new EmailVerificationResendRequest(UserFixture.EMAIL);
    }
}
