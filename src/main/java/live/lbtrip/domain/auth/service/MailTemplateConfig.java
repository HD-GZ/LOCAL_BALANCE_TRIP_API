package live.lbtrip.domain.auth.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import live.lbtrip.global.i18n.LocaleConfig;

@Configuration
public class MailTemplateConfig {

    @Bean
    public LocalizedMailTemplate emailVerificationMailTemplate(ResourceLoader resourceLoader) {
        return new LocalizedMailTemplate(
            "email-verification", LocaleConfig.SUPPORTED_LOCALES, LocaleConfig.DEFAULT_LOCALE, resourceLoader);
    }

    @Bean
    public LocalizedMailTemplate passwordResetMailTemplate(ResourceLoader resourceLoader) {
        return new LocalizedMailTemplate(
            "password-reset", LocaleConfig.SUPPORTED_LOCALES, LocaleConfig.DEFAULT_LOCALE, resourceLoader);
    }
}
