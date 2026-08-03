package live.lbtrip.domain.user.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Nested
    class 회원탈퇴 {

        @Test
        void 탈퇴하면_상태가_WITHDRAWN이_되고_탈퇴_시점이_기록된다() {
            User user = UserFixture.activeUser();
            LocalDateTime now = LocalDateTime.now();

            user.withdraw(now);

            assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
            assertThat(user.isWithdrawn()).isTrue();
            assertThat(user.getWithdrawnAt()).isEqualTo(now);
        }

        @Test
        void 이미_탈퇴한_회원이_다시_탈퇴하면_예외가_발생한다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now());

            assertThatThrownBy(() -> user.withdraw(LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }

        @Test
        void 유예기간_내에_철회하면_ACTIVE로_복원되고_탈퇴_시점이_초기화된다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(10));

            user.reinstate(LocalDateTime.now(), Duration.ofDays(30));

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getWithdrawnAt()).isNull();
        }

        @Test
        void 유예기간이_지난_회원은_철회할_수_없다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(40));

            assertThatThrownBy(() -> user.reinstate(LocalDateTime.now(), Duration.ofDays(30)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }

        @Test
        void 탈퇴하지_않은_회원은_철회할_수_없다() {
            User user = UserFixture.activeUser();

            assertThatThrownBy(() -> user.reinstate(LocalDateTime.now(), Duration.ofDays(30)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }

        @Test
        void 익명화하면_식별_정보가_덮어써지고_파기_시점이_기록된다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(40));
            LocalDateTime now = LocalDateTime.now();

            user.anonymize("anonymized-password", now);

            assertThat(user.getName()).isEqualTo("탈퇴회원");
            assertThat(user.getEmail()).startsWith("withdrawn.").endsWith("@deleted.local");
            assertThat(user.getPassword()).isEqualTo("anonymized-password");
            assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(UserFixture.BIRTH_DATE.getYear(), 1, 1));
            assertThat(user.getDeletedAt()).isEqualTo(now);
            assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        }

        @Test
        void 익명화된_회원은_철회할_수_없다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(40));
            user.anonymize("anonymized-password", LocalDateTime.now());

            assertThatThrownBy(() -> user.reinstate(LocalDateTime.now(), Duration.ofDays(365)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }
    }
}
