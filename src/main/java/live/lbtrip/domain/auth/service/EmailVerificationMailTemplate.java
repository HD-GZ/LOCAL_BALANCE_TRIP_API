package live.lbtrip.domain.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
final class EmailVerificationMailTemplate {

    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";

    private final String plainTextTemplate;
    private final String htmlTemplate;

    EmailVerificationMailTemplate(
        @Value("classpath:templates/email/email-verification.txt") Resource plainTextTemplateResource,
        @Value("classpath:templates/email/email-verification.html") Resource htmlTemplateResource
    ) {
        try {
            this.plainTextTemplate = plainTextTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            this.htmlTemplate = htmlTemplateResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("이메일 인증 템플릿을 불러올 수 없습니다.", exception);
        }
    }

    String plainText(String code) {
        return plainTextTemplate.replace(CODE_PLACEHOLDER, code);
    }

    String html(String code) {
        return htmlTemplate.replace(CODE_PLACEHOLDER, code);
    }
}
