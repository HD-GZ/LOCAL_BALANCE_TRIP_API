package live.lbtrip.domain.auth.service;

import java.io.IOException;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import live.lbtrip.domain.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final String MAIL_SEND_ENDPOINT = "mail/send";
	private static final int SUCCESS_STATUS_MIN = 200;
	private static final int SUCCESS_STATUS_MAX = 299;

	private final SendGrid sendGrid;
	private final String from;

	public EmailService(
		SendGrid sendGrid,
		@Value("${app.mail.from}") String from
	) {
		this.sendGrid = sendGrid;
		this.from = from;
	}

	public void sendVerificationEmail(User user, String code) {
		Mail mail = new Mail(
			new Email(from),
			"[로컬밸런스 트립] 이메일 인증을 완료해 주세요",
			new Email(user.getEmail()),
			new Content("text/plain", """
			로컬밸런스 트립 회원가입을 완료하려면 아래 인증 코드를 입력해 주세요.

			인증 코드: %s

			본인이 요청하지 않았다면 이 메일을 무시해 주세요.
			""".formatted(code))
		);

		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint(MAIL_SEND_ENDPOINT);
			request.setBody(mail.build());

			Response response = sendGrid.api(request);
			if (response.getStatusCode() < SUCCESS_STATUS_MIN || response.getStatusCode() > SUCCESS_STATUS_MAX) {
				throw new IllegalStateException(
					"SendGrid email send failed. statusCode=%d, body=%s".formatted(
						response.getStatusCode(),
						response.getBody()
					)
				);
			}
		} catch (IOException exception) {
			throw new IllegalStateException("SendGrid email send failed.", exception);
		}
	}
}
