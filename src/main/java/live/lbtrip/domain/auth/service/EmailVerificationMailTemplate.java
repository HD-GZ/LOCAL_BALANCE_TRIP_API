package live.lbtrip.domain.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
final class EmailVerificationMailTemplate {

    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";

    private final String htmlTemplate;

    EmailVerificationMailTemplate(
        @Value("classpath:templates/email/email-verification.html") Resource htmlTemplateResource
    ) {
        try {
            this.htmlTemplate = htmlTemplateResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("이메일 인증 HTML 템플릿을 불러올 수 없습니다.", exception);
        }
    }

    String plainText(String code) {
        return """
            로컬밸런스 트립 이메일 인증

            회원가입을 완료하려면 아래 인증번호를 입력해 주세요.

            인증번호: %s

            본인이 요청하지 않았다면 이 메일을 무시해 주세요.
            보안을 위해 인증번호를 다른 사람에게 공유하지 마세요.

            이 메일은 발신 전용입니다.
            """.formatted(code);
    }

    String html(String code) {
        return htmlTemplate.replace(CODE_PLACEHOLDER, code);
    }
}
