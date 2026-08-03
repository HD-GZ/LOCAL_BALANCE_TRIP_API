package live.lbtrip.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.UserFixture;

class PasswordResetTokenTest {

    private static final String CODE = "123456";
    private static final String RESET_TOKEN = "11111111-1111-1111-1111-111111111111";

    @Nested
    class 리셋_토큰_발급 {

        @Test
        void 유효한_코드면_리셋_토큰을_발급한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));

            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));

            assertThat(token.getResetToken()).isEqualTo(RESET_TOKEN);
        }

        @Test
        void 만료된_코드면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.minusMinutes(1));

            assertThatThrownBy(() -> token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }

        @Test
        void 이미_확인된_코드면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));

            assertThatThrownBy(() -> token.issueResetToken("other-token", now, now.plusMinutes(10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_CODE_USED);
        }
    }

    @Nested
    class 리셋_토큰_사용 {

        @Test
        void 유효한_리셋_토큰이면_사용_처리한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));

            token.use(now);

            assertThat(token.isUsed()).isTrue();
        }

        @Test
        void 만료된_리셋_토큰이면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.minusMinutes(1));

            assertThatThrownBy(() -> token.use(now))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        @Test
        void 이미_사용한_리셋_토큰이면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));
            token.use(now);

            assertThatThrownBy(() -> token.use(now))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_USED);
        }
    }
}
