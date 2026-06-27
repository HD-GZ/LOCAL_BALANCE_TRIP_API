package live.lbtrip.support.fixture;

import java.time.LocalDate;

import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.domain.user.model.User;

public final class UserFixture {

    public static final String NAME = "홍길동";
    public static final String EMAIL = "user@example.com";
    public static final String PASSWORD = "password1";
    public static final String ENCODED_PASSWORD = "encoded-password";
    public static final LocalDate BIRTH_DATE = LocalDate.of(1999, 1, 1);
    public static final Gender GENDER = Gender.NOT_SPECIFIED;

    private UserFixture() {
    }

    public static User user() {
        return User.create(
            NAME,
            EMAIL,
            ENCODED_PASSWORD,
            BIRTH_DATE,
            GENDER,
            true,
            true,
            false
        );
    }

    public static User user(String email) {
        return User.create(
            NAME,
            email,
            ENCODED_PASSWORD,
            BIRTH_DATE,
            GENDER,
            true,
            true,
            false
        );
    }

    public static User activeUser() {
        User user = user();
        user.verifyEmail();
        return user;
    }
}
