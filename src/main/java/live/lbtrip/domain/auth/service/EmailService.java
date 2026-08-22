package live.lbtrip.domain.auth.service;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

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

    private final JavaMailSender mailSender;
    private final String from;
    private final String fromName;
    private final EmailVerificationMailTemplate mailTemplate;
    private final PasswordResetMailTemplate passwordResetMailTemplate;
    private final MessageResolver messageResolver;

    public EmailService(
        JavaMailSender mailSender,
        @Value("${app.mail.from}") String from,
        @Value("${app.mail.from-name}") String fromName,
        EmailVerificationMailTemplate mailTemplate,
        PasswordResetMailTemplate passwordResetMailTemplate,
        MessageResolver messageResolver
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
        this.mailTemplate = mailTemplate;
        this.passwordResetMailTemplate = passwordResetMailTemplate;
        this.messageResolver = messageResolver;
    }

    public void sendVerificationEmail(String toEmail, String code) {
        Locale locale = messageResolver.currentLocale();
        send(toEmail, messageResolver.resolve(locale, "mail.emailVerification.subject"),
            mailTemplate.plainText(code, locale), mailTemplate.html(code, locale));
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        Locale locale = messageResolver.currentLocale();
        send(toEmail, messageResolver.resolve(locale, "mail.passwordReset.subject"),
            passwordResetMailTemplate.plainText(code, locale), passwordResetMailTemplate.html(code, locale));
    }

    private void send(String toEmail, String subject, String plainText, String html) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODING);
            helper.setFrom(from, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException | MailException exception) {
            throw BusinessException.of(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
