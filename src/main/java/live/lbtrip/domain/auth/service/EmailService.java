package live.lbtrip.domain.auth.service;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.MessageResolver;

@Service
public class EmailService {

    private static final String ENCODING = "UTF-8";
    private static final String FROM_NAME_KEY = "mail.fromName";
    private static final String VERIFICATION_SUBJECT_KEY = "mail.emailVerification.subject";
    private static final String PASSWORD_RESET_SUBJECT_KEY = "mail.passwordReset.subject";

    private final JavaMailSender mailSender;
    private final String from;
    private final MessageResolver messageResolver;
    private final LocalizedMailTemplate emailVerificationMailTemplate;
    private final LocalizedMailTemplate passwordResetMailTemplate;

    public EmailService(
        JavaMailSender mailSender,
        @Value("${app.mail.from}") String from,
        MessageResolver messageResolver,
        @Qualifier("emailVerificationMailTemplate") LocalizedMailTemplate emailVerificationMailTemplate,
        @Qualifier("passwordResetMailTemplate") LocalizedMailTemplate passwordResetMailTemplate
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.messageResolver = messageResolver;
        this.emailVerificationMailTemplate = emailVerificationMailTemplate;
        this.passwordResetMailTemplate = passwordResetMailTemplate;
    }

    public void sendVerificationEmail(String toEmail, String code) {
        Locale locale = messageResolver.currentLocale();
        send(toEmail, locale, messageResolver.resolve(locale, VERIFICATION_SUBJECT_KEY),
            emailVerificationMailTemplate.plainText(locale, code), emailVerificationMailTemplate.html(locale, code));
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        Locale locale = messageResolver.currentLocale();
        send(toEmail, locale, messageResolver.resolve(locale, PASSWORD_RESET_SUBJECT_KEY),
            passwordResetMailTemplate.plainText(locale, code), passwordResetMailTemplate.html(locale, code));
    }

    private void send(String toEmail, Locale locale, String subject, String plainText, String html) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODING);
            helper.setFrom(from, messageResolver.resolve(locale, FROM_NAME_KEY));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException | MailException exception) {
            throw BusinessException.of(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
