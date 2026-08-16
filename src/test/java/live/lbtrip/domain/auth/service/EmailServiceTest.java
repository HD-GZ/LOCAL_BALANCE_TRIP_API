package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
    private static final String PLAIN_TEXT = "인증번호는 123456 입니다.";
    private static final String HTML = "<p>인증번호는 123456 입니다.</p>";

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MessageResolver messageResolver;

    @Mock
    private LocalizedMailTemplate mailTemplate;

    @Mock
    private LocalizedMailTemplate passwordResetMailTemplate;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(
            mailSender,
            FROM,
            messageResolver,
            mailTemplate,
            passwordResetMailTemplate
        );
    }

    private MimeMessage emptyMessage() {
        return new MimeMessage((Session) null);
    }

    @Nested
    class 인증번호_발송 {

        @Test
        void 수신자와_로케일별_제목을_담아_메일을_발송한다() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(emptyMessage());
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(messageResolver.resolve(Locale.ENGLISH, "mail.fromName")).thenReturn("Local Balance Trip");
            when(messageResolver.resolve(Locale.ENGLISH, "mail.emailVerification.subject"))
                .thenReturn("[Local Balance Trip] Your email verification code");
            when(mailTemplate.plainText(Locale.ENGLISH, AuthRequestFixture.VERIFICATION_CODE)).thenReturn(PLAIN_TEXT);
            when(mailTemplate.html(Locale.ENGLISH, AuthRequestFixture.VERIFICATION_CODE)).thenReturn(HTML);

            emailService.sendVerificationEmail(UserFixture.EMAIL, AuthRequestFixture.VERIFICATION_CODE);

            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(messageCaptor.capture());
            MimeMessage sent = messageCaptor.getValue();
            assertThat(sent.getAllRecipients()[0].toString()).isEqualTo(UserFixture.EMAIL);
            assertThat(sent.getSubject()).isEqualTo("[Local Balance Trip] Your email verification code");
            assertThat(sent.getFrom()[0].toString()).contains("Local Balance Trip");
        }

        @Test
        void 발송에_실패하면_예외를_던진다() {
            when(mailSender.createMimeMessage()).thenReturn(emptyMessage());
            when(messageResolver.currentLocale()).thenReturn(Locale.KOREAN);
            when(messageResolver.resolve(Locale.KOREAN, "mail.fromName")).thenReturn("로컬밸런스 트립");
            when(messageResolver.resolve(Locale.KOREAN, "mail.emailVerification.subject"))
                .thenReturn("[로컬밸런스 트립] 이메일 인증번호를 안내드립니다");
            when(mailTemplate.plainText(Locale.KOREAN, AuthRequestFixture.VERIFICATION_CODE)).thenReturn(PLAIN_TEXT);
            when(mailTemplate.html(Locale.KOREAN, AuthRequestFixture.VERIFICATION_CODE)).thenReturn(HTML);
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
        void 수신자와_로케일별_제목을_담아_메일을_발송한다() throws Exception {
            when(mailSender.createMimeMessage()).thenReturn(emptyMessage());
            when(messageResolver.currentLocale()).thenReturn(Locale.KOREAN);
            when(messageResolver.resolve(Locale.KOREAN, "mail.fromName")).thenReturn("로컬밸런스 트립");
            when(messageResolver.resolve(Locale.KOREAN, "mail.passwordReset.subject"))
                .thenReturn("[로컬밸런스 트립] 비밀번호 재설정 인증번호를 안내드립니다");
            when(passwordResetMailTemplate.plainText(Locale.KOREAN, AuthRequestFixture.VERIFICATION_CODE))
                .thenReturn(PLAIN_TEXT);
            when(passwordResetMailTemplate.html(Locale.KOREAN, AuthRequestFixture.VERIFICATION_CODE))
                .thenReturn(HTML);

            emailService.sendPasswordResetEmail(UserFixture.EMAIL, AuthRequestFixture.VERIFICATION_CODE);

            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(messageCaptor.capture());
            MimeMessage sent = messageCaptor.getValue();
            assertThat(sent.getAllRecipients()[0].toString()).isEqualTo(UserFixture.EMAIL);
            assertThat(sent.getSubject()).isEqualTo("[로컬밸런스 트립] 비밀번호 재설정 인증번호를 안내드립니다");
        }
    }
}
