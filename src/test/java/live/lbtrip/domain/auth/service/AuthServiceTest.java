package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import live.lbtrip.domain.auth.dto.response.LoginResponse;
import live.lbtrip.domain.auth.dto.response.SignupResponse;
import live.lbtrip.domain.auth.dto.response.TokenResponse;
import live.lbtrip.domain.auth.model.RefreshToken;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthRequestFixture;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Duration GRACE_PERIOD = Duration.ofDays(30);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepository,
            passwordEncoder,
            emailVerificationService,
            jwtTokenProvider,
            refreshTokenService,
            GRACE_PERIOD
        );
    }

    @Nested
    class 회원가입 {

        @Test
        void 회원가입을_처리하고_이메일_인증을_발급한다() {
            User savedUser = UserFixture.user();
            when(userRepository.existsByEmail(UserFixture.EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(UserFixture.PASSWORD)).thenReturn(UserFixture.ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(emailVerificationService.issue(savedUser)).thenReturn(AuthResponseFixture.VERIFICATION_CODE_EXPIRES_IN);

            SignupResponse response = authService.signup(AuthRequestFixture.signupRequest());

            assertThat(response.email()).isEqualTo(UserFixture.EMAIL);
            assertThat(response.status()).isEqualTo(UserStatus.PENDING_EMAIL_VERIFICATION);
            assertThat(response.verificationCodeExpiresIn())
                .isEqualTo(AuthResponseFixture.VERIFICATION_CODE_EXPIRES_IN);
            verify(emailVerificationService).issue(savedUser);
        }

        @Test
        void 이미_사용_중인_이메일이면_예외를_던진다() {
            when(userRepository.existsByEmail(UserFixture.EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.signup(AuthRequestFixture.signupRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Nested
    class 로그인 {

        @Test
        void 비밀번호가_일치하지_않으면_예외를_던진다() {
            User user = UserFixture.activeUser();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(UserFixture.PASSWORD, user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(AuthRequestFixture.loginRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        @Test
        void 이메일_인증이_완료되지_않았으면_예외를_던진다() {
            User user = UserFixture.user();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(UserFixture.PASSWORD, user.getPassword())).thenReturn(true);

            assertThatThrownBy(() -> authService.login(AuthRequestFixture.loginRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        @Test
        void 로그인에_성공하면_토큰을_반환한다() {
            User user = UserFixture.activeUser();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(UserFixture.PASSWORD, user.getPassword())).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(user)).thenReturn(TokenFixture.ACCESS_TOKEN);
            when(refreshTokenService.issue(user)).thenReturn(TokenFixture.REFRESH_TOKEN);

            LoginResponse response = authService.login(AuthRequestFixture.loginRequest());

            assertThat(response.accessToken()).isEqualTo(TokenFixture.ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(TokenFixture.REFRESH_TOKEN);
        }

        @Test
        void 유예기간_내의_탈퇴_회원이_로그인하면_탈퇴가_철회된다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(10));
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(UserFixture.PASSWORD, UserFixture.ENCODED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(user)).thenReturn(TokenFixture.ACCESS_TOKEN);
            when(refreshTokenService.issue(user)).thenReturn(TokenFixture.REFRESH_TOKEN);

            LoginResponse response = authService.login(AuthRequestFixture.loginRequest());

            assertThat(response.reinstated()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getWithdrawnAt()).isNull();
        }

        @Test
        void 유예기간이_지난_탈퇴_회원은_로그인할_수_없다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(40));
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(UserFixture.PASSWORD, UserFixture.ENCODED_PASSWORD)).thenReturn(true);

            assertThatThrownBy(() -> authService.login(AuthRequestFixture.loginRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }

        @Test
        void 일반_회원이_로그인하면_reinstated는_false다() {
            User user = UserFixture.activeUser();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(UserFixture.PASSWORD, UserFixture.ENCODED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(user)).thenReturn(TokenFixture.ACCESS_TOKEN);
            when(refreshTokenService.issue(user)).thenReturn(TokenFixture.REFRESH_TOKEN);

            LoginResponse response = authService.login(AuthRequestFixture.loginRequest());

            assertThat(response.reinstated()).isFalse();
        }
    }

    @Nested
    class 토큰 {

        @Test
        void 리프레시_토큰으로_액세스_토큰을_재발급한다() {
            User user = UserFixture.activeUser();
            RefreshToken refreshToken = RefreshToken.create(
                user,
                TokenFixture.REFRESH_TOKEN,
                java.time.LocalDateTime.now().plusMinutes(10)
            );
            when(refreshTokenService.findUsable(TokenFixture.REFRESH_TOKEN)).thenReturn(refreshToken);
            when(jwtTokenProvider.createAccessToken(user)).thenReturn(TokenFixture.NEW_ACCESS_TOKEN);

            TokenResponse response = authService.refreshToken(AuthRequestFixture.tokenRefreshRequest());

            assertThat(response.accessToken()).isEqualTo(TokenFixture.NEW_ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(TokenFixture.REFRESH_TOKEN);
        }

        @Test
        void 로그아웃하면_리프레시_토큰을_삭제한다() {
            authService.logout(AuthResponseFixture.USER_ID);

            verify(refreshTokenService).deleteByUserId(AuthResponseFixture.USER_ID);
        }
    }
}
