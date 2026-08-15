package live.lbtrip.domain.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetMailTemplate {

    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";

    private final String plainTextTemplate;
    private final String htmlTemplate;

    public PasswordResetMailTemplate(
        @Value("classpath:templates/email/password-reset.txt") Resource plainTextTemplateResource,
        @Value("classpath:templates/email/password-reset.html") Resource htmlTemplateResource
    ) {
        try {
            this.plainTextTemplate = plainTextTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            this.htmlTemplate = htmlTemplateResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("비밀번호 재설정 템플릿을 불러올 수 없습니다.", exception);
        }
    }

    public String plainText(String code) {
        return plainTextTemplate.replace(CODE_PLACEHOLDER, code);
    }

    public String html(String code) {
        return htmlTemplate.replace(CODE_PLACEHOLDER, code);
    }
}
