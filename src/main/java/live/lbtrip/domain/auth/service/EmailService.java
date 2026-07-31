package live.lbtrip.domain.auth.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@Service
public class EmailService {

    private static final String MAIL_SEND_ENDPOINT = "mail/send";
    private static final int SUCCESS_STATUS_MIN = 200;
    private static final int SUCCESS_STATUS_MAX = 299;

    private final SendGrid sendGrid;
    private final String from;
    private final String fromName;

    public EmailService(
        SendGrid sendGrid,
        @Value("${app.mail.from}") String from,
        @Value("${app.mail.from-name}") String fromName
    ) {
        this.sendGrid = sendGrid;
        this.from = from;
        this.fromName = fromName;
    }

    public void sendVerificationEmail(String toEmail, String code) {
        Mail mail = new Mail(
            new Email(from, fromName),
            "[로컬밸런스 트립] 이메일 인증번호를 안내드립니다",
            new Email(toEmail),
            new Content("text/plain", EmailVerificationMailTemplate.plainText(code))
        );
        mail.addContent(new Content("text/html", EmailVerificationMailTemplate.html(code)));

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND_ENDPOINT);
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            if (response.getStatusCode() < SUCCESS_STATUS_MIN || response.getStatusCode() > SUCCESS_STATUS_MAX) {
                throw BusinessException.of(ErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (IOException exception) {
            throw BusinessException.of(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
