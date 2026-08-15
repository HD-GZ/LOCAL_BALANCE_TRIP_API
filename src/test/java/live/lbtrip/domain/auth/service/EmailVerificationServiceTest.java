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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.auth.dto.response.EmailVerificationResponse;
import live.lbtrip.domain.auth.model.SignupVerificationToken;
import live.lbtrip.domain.auth.repository.SignupVerificationTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthRequestFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final Duration TOKEN_EXPIRATION = Duration.ofDays(1);

    @Mock
    private SignupVerificationTokenRepository tokenRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationCodeGenerator codeGenerator;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(
            tokenRepository,
            userFinder,
            emailService,
            codeGenerator,
            TOKEN_EXPIRATION
        );
    }

    @Nested
    class 발급 {

        @Test
        void 인증_코드를_발급하고_메일을_발송한다() {
            User user = UserFixture.user();
            when(codeGenerator.generate()).thenReturn(AuthRequestFixture.VERIFICATION_CODE);

            long expiresIn = emailVerificationService.issue(user);

            ArgumentCaptor<SignupVerificationToken> tokenCaptor = ArgumentCaptor.forClass(SignupVerificationToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getCode()).isEqualTo(AuthRequestFixture.VERIFICATION_CODE);
            verify(emailService).sendVerificationEmail(UserFixture.EMAIL, AuthRequestFixture.VERIFICATION_CODE);
            assertThat(expiresIn).isEqualTo(TOKEN_EXPIRATION.toSeconds());
        }
    }

    @Nested
    class 확인 {

        @Test
        void 인증_코드를_확인하면_사용자를_활성화한다() {
            User user = UserFixture.user();
            SignupVerificationToken token = SignupVerificationToken.create(
                user,
                AuthRequestFixture.VERIFICATION_CODE,
                LocalDateTime.now().plusMinutes(10)
            );
            when(tokenRepository.findByCode(AuthRequestFixture.VERIFICATION_CODE)).thenReturn(Optional.of(token));

            EmailVerificationResponse response = emailVerificationService.confirm(
                AuthRequestFixture.emailVerificationConfirmRequest()
            );

            assertThat(token.isUsed()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(response.email()).isEqualTo(UserFixture.EMAIL);
            assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        void 존재하지_않는_인증_코드면_예외를_던진다() {
            when(tokenRepository.findByCode(AuthRequestFixture.VERIFICATION_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> emailVerificationService.confirm(
                AuthRequestFixture.emailVerificationConfirmRequest()
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND);
        }
    }

    @Nested
    class 재발송 {

        @Test
        void 인증_코드를_재발송한다() {
            User user = UserFixture.user();
            when(userFinder.findByEmail(UserFixture.EMAIL)).thenReturn(user);
            when(codeGenerator.generate()).thenReturn(AuthRequestFixture.VERIFICATION_CODE);

            EmailVerificationResponse response = emailVerificationService.resend(
                AuthRequestFixture.emailVerificationResendRequest()
            );

            verify(tokenRepository).save(any(SignupVerificationToken.class));
            verify(emailService).sendVerificationEmail(UserFixture.EMAIL, AuthRequestFixture.VERIFICATION_CODE);
            assertThat(response.email()).isEqualTo(UserFixture.EMAIL);
            assertThat(response.status()).isEqualTo(UserStatus.PENDING_EMAIL_VERIFICATION);
        }

        @Test
        void 이미_인증된_사용자면_예외를_던진다() {
            User user = UserFixture.activeUser();
            when(userFinder.findByEmail(UserFixture.EMAIL)).thenReturn(user);

            assertThatThrownBy(() -> emailVerificationService.resend(
                AuthRequestFixture.emailVerificationResendRequest()
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        @Test
        void 탈퇴한_회원이면_예외를_던진다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now());
            when(userFinder.findByEmail(UserFixture.EMAIL)).thenReturn(user);

            assertThatThrownBy(() -> emailVerificationService.resend(
                AuthRequestFixture.emailVerificationResendRequest()
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }
    }
}
