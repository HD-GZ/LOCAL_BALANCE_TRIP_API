package live.lbtrip.domain.user.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.UserFixture;
import live.lbtrip.support.fixture.UserRequestFixture;

class UserTest {

    @Nested
    class 생성 {

        @Test
        void 사용자를_생성한다() {
            User user = UserFixture.user();

            assertThat(user.getName()).isEqualTo(UserFixture.NAME);
            assertThat(user.getEmail()).isEqualTo(UserFixture.EMAIL);
            assertThat(user.getPassword()).isEqualTo(UserFixture.ENCODED_PASSWORD);
            assertThat(user.getBirthDate()).isEqualTo(UserFixture.BIRTH_DATE);
            assertThat(user.getGender()).isEqualTo(UserFixture.GENDER);
            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_EMAIL_VERIFICATION);
            assertThat(user.isActive()).isFalse();
            assertThat(user.isTermsAgreed()).isTrue();
            assertThat(user.isPrivacyAgreed()).isTrue();
            assertThat(user.isMarketingAgreed()).isFalse();
        }

        @Test
        void 서비스_이용약관에_동의하지_않으면_예외를_던진다() {
            assertThatThrownBy(() -> User.create(
                UserFixture.NAME,
                UserFixture.EMAIL,
                UserFixture.ENCODED_PASSWORD,
                UserFixture.BIRTH_DATE,
                UserFixture.GENDER,
                false,
                true,
                false
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
        }

        @Test
        void 개인정보_수집_이용에_동의하지_않으면_예외를_던진다() {
            assertThatThrownBy(() -> User.create(
                UserFixture.NAME,
                UserFixture.EMAIL,
                UserFixture.ENCODED_PASSWORD,
                UserFixture.BIRTH_DATE,
                UserFixture.GENDER,
                true,
                false,
                false
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
        }
    }

    @Nested
    class 정보_수정 {

        @Test
        void 이름_생년월일_성별을_수정한다() {
            User user = UserFixture.activeUser();

            user.update(UserRequestFixture.NEW_NAME, UserRequestFixture.NEW_BIRTH_DATE, UserRequestFixture.NEW_GENDER);

            assertThat(user.getName()).isEqualTo(UserRequestFixture.NEW_NAME);
            assertThat(user.getBirthDate()).isEqualTo(UserRequestFixture.NEW_BIRTH_DATE);
            assertThat(user.getGender()).isEqualTo(UserRequestFixture.NEW_GENDER);
            assertThat(user.getPassword()).isEqualTo(UserFixture.ENCODED_PASSWORD);
        }

        @Test
        void 비밀번호를_변경한다() {
            User user = UserFixture.activeUser();

            user.changePassword(UserRequestFixture.NEW_ENCODED_PASSWORD);

            assertThat(user.getPassword()).isEqualTo(UserRequestFixture.NEW_ENCODED_PASSWORD);
        }
    }

    @Nested
    class 이메일_인증 {

        @Test
        void 이메일_인증을_완료하면_사용자가_활성화된다() {
            User user = UserFixture.user();

            user.verifyEmail();

            assertThat(user.isActive()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }
}
