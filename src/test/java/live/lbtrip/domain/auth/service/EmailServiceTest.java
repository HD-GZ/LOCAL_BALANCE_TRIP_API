package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.MessageResolver;
import live.lbtrip.support.fixture.AuthRequestFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String FROM = "no-reply@lb-trip.live";
    private static final String FROM_NAME = "로컬밸런스 트립";
    private static final String PLAIN_TEXT = "인증번호는 123456 입니다.";
    private static final String HTML = "<p>인증번호는 123456 입니다.</p>";
    private static final String VERIFICATION_SUBJECT = "[로컬밸런스 트립] 이메일 인증번호를 안내드립니다";
    private static final String VERIFICATION_SUBJECT_EN = "[Local Balance Trip] Your email verification code";
    private static final String PASSWORD_RESET_SUBJECT = "[로컬밸런스 트립] 비밀번호 재설정 인증번호를 안내드립니다";

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailVerificationMailTemplate mailTemplate;

    @Mock
    private PasswordResetMailTemplate passwordResetMailTemplate;

    @Mock
    private MessageResolver messageResolver;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(
            mailSender,
            FROM,
            FROM_NAME,
            mailTemplate,
            passwordResetMailTemplate,
            messageResolver
        );
        lenient().when(messageResolver.currentLocale()).thenReturn(Locale.KOREAN);
        lenient().when(messageResolver.resolve(Locale.KOREAN, "mail.emailVerification.subject"))
            .thenReturn(VERIFICATION_SUBJECT);
        lenient().when(messageResolver.resolve(Locale.KOREAN, "mail.passwordReset.subject"))
            .thenReturn(PASSWORD_RESET_SUBJECT);
    }

    private MimeMessage emptyMessage() {
        return new MimeMessage((Session) null);
    }

    @Nested
    class 인증번호_발송 {

        @Test
        void 수신자와_제목을_담아_메일을_발송한다() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(emptyMessage());
            when(mailTemplate.plainText(AuthRequestFixture.VERIFICATION_CODE, Locale.KOREAN)).thenReturn(PLAIN_TEXT);
            when(mailTemplate.html(AuthRequestFixture.VERIFICATION_CODE, Locale.KOREAN)).thenReturn(HTML);

            emailService.sendVerificationEmail(UserFixture.EMAIL, AuthRequestFixture.VERIFICATION_CODE);

            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(messageCaptor.capture());
            MimeMessage sent = messageCaptor.getValue();
            assertThat(sent.getAllRecipients()[0].toString()).isEqualTo(UserFixture.EMAIL);
            assertThat(sent.getSubject()).isEqualTo(VERIFICATION_SUBJECT);
        }

        @Test
        void 영어_로케일이면_영문_제목과_영문_템플릿으로_발송한다() throws Exception {
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(messageResolver.resolve(Locale.ENGLISH, "mail.emailVerification.subject"))
                .thenReturn(VERIFICATION_SUBJECT_EN);
            when(mailSender.createMimeMessage()).thenReturn(emptyMessage());
            when(mailTemplate.plainText(AuthRequestFixture.VERIFICATION_CODE, Locale.ENGLISH)).thenReturn(PLAIN_TEXT);
            when(mailTemplate.html(AuthRequestFixture.VERIFICATION_CODE, Locale.ENGLISH)).thenReturn(HTML);

            emailService.sendVerificationEmail(UserFixture.EMAIL, AuthRequestFixture.VERIFICATION_CODE);

            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(messageCaptor.capture());
            assertThat(messageCaptor.getValue().getSubject()).isEqualTo(VERIFICATION_SUBJECT_EN);
            verify(mailTemplate).plainText(AuthRequestFixture.VERIFICATION_CODE, Locale.ENGLISH);
            verify(mailTemplate).html(AuthRequestFixture.VERIFICATION_CODE, Locale.ENGLISH);
        }

        @Test
        void 발송에_실패하면_예외를_던진다() {
            when(mailSender.createMimeMessage()).thenReturn(emptyMessage());
            when(mailTemplate.plainText(AuthRequestFixture.VERIFICATION_CODE, Locale.KOREAN)).thenReturn(PLAIN_TEXT);
            when(mailTemplate.html(AuthRequestFixture.VERIFICATION_CODE, Locale.KOREAN)).thenReturn(HTML);
            doThrow(new MailSendException("smtp unavailable"))
                .when(mailSender).send(any(MimeMessage.class));

            assertThatThrownBy(() -> emailService.sendVerificationEmail(
                UserFixture.EMAIL,
                AuthRequestFixture.VERIFICATION_CODE
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Nested
    class 비밀번호_재설정_발송 {

        @Test
        void 수신자와_제목을_담아_메일을_발송한다() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(emptyMessage());
            when(passwordResetMailTemplate.plainText(AuthRequestFixture.VERIFICATION_CODE, Locale.KOREAN))
                .thenReturn(PLAIN_TEXT);
            when(passwordResetMailTemplate.html(AuthRequestFixture.VERIFICATION_CODE, Locale.KOREAN))
                .thenReturn(HTML);

            emailService.sendPasswordResetEmail(UserFixture.EMAIL, AuthRequestFixture.VERIFICATION_CODE);

            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(messageCaptor.capture());
            MimeMessage sent = messageCaptor.getValue();
            assertThat(sent.getAllRecipients()[0].toString()).isEqualTo(UserFixture.EMAIL);
            assertThat(sent.getSubject()).isEqualTo(PASSWORD_RESET_SUBJECT);
        }
    }
}
