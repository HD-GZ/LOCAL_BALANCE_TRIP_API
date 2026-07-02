package live.lbtrip.support.fixture;

import live.lbtrip.domain.user.dto.response.UserResponse;
import live.lbtrip.domain.user.model.UserStatus;

public final class UserResponseFixture {

    public static final boolean MARKETING_AGREED = false;

    private UserResponseFixture() {
    }

    public static UserResponse userResponse() {
        return new UserResponse(
            AuthResponseFixture.USER_ID,
            UserFixture.NAME,
            UserFixture.EMAIL,
            UserFixture.BIRTH_DATE,
            UserFixture.GENDER,
            UserStatus.ACTIVE,
            MARKETING_AGREED
        );
    }
}
