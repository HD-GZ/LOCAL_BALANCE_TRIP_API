package live.lbtrip.domain.auth.service;

import live.lbtrip.domain.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final String from;

	public EmailService(
		JavaMailSender mailSender,
		@Value("${app.mail.from}") String from
	) {
		this.mailSender = mailSender;
		this.from = from;
	}

	public void sendVerificationEmail(User user, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(user.getEmail());
		message.setSubject("[로컬밸런스 트립] 이메일 인증을 완료해 주세요");
		message.setText("""
			로컬밸런스 트립 회원가입을 완료하려면 아래 인증 코드를 입력해 주세요.

			인증 코드: %s

			본인이 요청하지 않았다면 이 메일을 무시해 주세요.
			""".formatted(code));

		mailSender.send(message);
	}
}
