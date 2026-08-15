package live.lbtrip.support.fixture;

import java.time.LocalDate;

import live.lbtrip.domain.user.dto.request.UserUpdateRequest;
import live.lbtrip.domain.user.model.Gender;

public final class UserRequestFixture {

    public static final String NEW_NAME = "김철수";
    public static final LocalDate NEW_BIRTH_DATE = LocalDate.of(1990, 3, 15);
    public static final Gender NEW_GENDER = Gender.MALE;
    public static final String NEW_PASSWORD = "newpassword1";
    public static final String NEW_ENCODED_PASSWORD = "new-encoded-password";

    private UserRequestFixture() {
    }

    public static UserUpdateRequest userUpdateRequest() {
        return new UserUpdateRequest(NEW_NAME, NEW_BIRTH_DATE, NEW_GENDER, null, null);
    }

    public static UserUpdateRequest userUpdateRequestWithPassword() {
        return new UserUpdateRequest(NEW_NAME, NEW_BIRTH_DATE, NEW_GENDER, NEW_PASSWORD, NEW_PASSWORD);
    }
}
