package live.lbtrip.domain.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationMailTemplate {

    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";

    private final String plainTextTemplate;
    private final String htmlTemplate;
    private final String plainTextTemplateEn;
    private final String htmlTemplateEn;

    public EmailVerificationMailTemplate(
        @Value("classpath:templates/email/email-verification.txt") Resource plainTextTemplateResource,
        @Value("classpath:templates/email/email-verification.html") Resource htmlTemplateResource,
        @Value("classpath:templates/email/email-verification_en.txt") Resource plainTextTemplateEnResource,
        @Value("classpath:templates/email/email-verification_en.html") Resource htmlTemplateEnResource
    ) {
        try {
            this.plainTextTemplate = plainTextTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            this.htmlTemplate = htmlTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            this.plainTextTemplateEn = plainTextTemplateEnResource.getContentAsString(StandardCharsets.UTF_8);
            this.htmlTemplateEn = htmlTemplateEnResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("이메일 인증 템플릿을 불러올 수 없습니다.", exception);
        }
    }

    public String plainText(String code, Locale locale) {
        return templateFor(plainTextTemplate, plainTextTemplateEn, locale).replace(CODE_PLACEHOLDER, code);
    }

    public String html(String code, Locale locale) {
        return templateFor(htmlTemplate, htmlTemplateEn, locale).replace(CODE_PLACEHOLDER, code);
    }

    private String templateFor(String korean, String english, Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? english : korean;
    }
}
