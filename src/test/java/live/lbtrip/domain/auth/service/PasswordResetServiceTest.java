package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.model.PasswordResetToken;
import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PasswordResetFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(10);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordResetCodeGenerator codeGenerator;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
            userRepository,
            tokenRepository,
            codeGenerator,
            emailService,
            passwordEncoder,
            refreshTokenService,
            CODE_EXPIRATION,
            TOKEN_EXPIRATION
        );
    }

    @Nested
    class 인증_코드_요청 {

        @Test
        void 인증_코드를_발급하고_메일을_발송한다() {
            User user = UserFixture.activeUser();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(codeGenerator.generate()).thenReturn(PasswordResetFixture.CODE);

            PasswordResetCodeResponse response =
                passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL));

            ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(captor.capture());
            assertThat(captor.getValue().getCode()).isEqualTo(PasswordResetFixture.CODE);
            verify(emailService).sendPasswordResetEmail(UserFixture.EMAIL, PasswordResetFixture.CODE);
            assertThat(response.expiresIn()).isEqualTo(CODE_EXPIRATION.toSeconds());
        }

        @Test
        void 가입되지_않은_이메일이면_예외가_발생한다() {
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 탈퇴한_회원이면_예외가_발생한다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now());
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }

        @Test
        void 이메일_미인증_회원이면_예외가_발생한다() {
            User user = UserFixture.user();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    @Nested
    class 인증_코드_확인 {

        @Test
        void 코드가_유효하면_리셋_토큰을_발급한다() {
            User user = UserFixture.activeUser();
            PasswordResetToken token =
                PasswordResetToken.create(user, PasswordResetFixture.CODE, LocalDateTime.now().plusMinutes(10));
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(tokenRepository.findFirstByUserIdAndCodeOrderByIdDesc(user.getId(), PasswordResetFixture.CODE))
                .thenReturn(Optional.of(token));

            PasswordResetTokenResponse response = passwordResetService.confirm(
                new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE));

            assertThat(response.resetToken()).isEqualTo(token.getResetToken());
            assertThat(token.getResetToken()).isNotNull();
            assertThat(response.expiresIn()).isEqualTo(TOKEN_EXPIRATION.toSeconds());
        }

        @Test
        void 코드를_찾을_수_없으면_예외가_발생한다() {
            User user = UserFixture.activeUser();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(tokenRepository.findFirstByUserIdAndCodeOrderByIdDesc(user.getId(), PasswordResetFixture.CODE))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.confirm(
                    new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_CODE_NOT_FOUND);
        }
    }

    @Nested
    class 비밀번호_재설정 {

        @Test
        void 비밀번호를_변경하고_리프레시_토큰을_폐기한다() {
            User user = UserFixture.activeUser();
            PasswordResetToken token =
                PasswordResetToken.create(user, PasswordResetFixture.CODE, LocalDateTime.now().plusMinutes(10));
            token.issueResetToken(PasswordResetFixture.RESET_TOKEN, LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
            when(tokenRepository.findByResetToken(PasswordResetFixture.RESET_TOKEN)).thenReturn(Optional.of(token));
            when(passwordEncoder.encode(PasswordResetFixture.NEW_PASSWORD)).thenReturn("encoded-new-password");

            passwordResetService.reset(
                new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD));

            assertThat(token.isUsed()).isTrue();
            assertThat(user.getPassword()).isEqualTo("encoded-new-password");
            verify(refreshTokenService).deleteByUserId(user.getId());
        }

        @Test
        void 리셋_토큰을_찾을_수_없으면_예외가_발생한다() {
            when(tokenRepository.findByResetToken(PasswordResetFixture.RESET_TOKEN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.reset(
                    new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND);
        }
    }
}
