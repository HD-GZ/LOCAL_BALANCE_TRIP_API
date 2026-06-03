package live.lbtrip.domain.auth;

import live.lbtrip.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final String from;
	private final String frontendBaseUrl;

	public EmailService(
		JavaMailSender mailSender,
		@Value("${app.mail.from}") String from,
		@Value("${app.frontend.base-url}") String frontendBaseUrl
	) {
		this.mailSender = mailSender;
		this.from = from;
		this.frontendBaseUrl = frontendBaseUrl;
	}

	public void sendVerificationEmail(User user, String token) {
		String verificationUrl = frontendBaseUrl + "/email-verification?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(user.getEmail());
		message.setSubject("[로컬밸런스 트립] 이메일 인증을 완료해 주세요");
		message.setText("""
			로컬밸런스 트립 회원가입을 완료하려면 아래 링크를 눌러 이메일 인증을 완료해 주세요.

			%s

			본인이 요청하지 않았다면 이 메일을 무시해 주세요.
			""".formatted(verificationUrl));

		mailSender.send(message);
	}
}
