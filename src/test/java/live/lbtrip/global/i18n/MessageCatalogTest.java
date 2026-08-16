package live.lbtrip.global.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;

import live.lbtrip.domain.home.model.PropensityFactor;
import live.lbtrip.global.error.ErrorCode;

class MessageCatalogTest {

    private static final Locale[] LOCALES = {Locale.KOREAN, Locale.ENGLISH};

    private final ResourceBundleMessageSource messageSource = messageSource();

    private static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }

    private void assertKeyExistsInAllLocales(String key) {
        for (Locale locale : LOCALES) {
            try {
                assertThat(messageSource.getMessage(key, null, locale)).isNotBlank();
            } catch (NoSuchMessageException e) {
                throw new AssertionError("missing key '%s' for locale %s".formatted(key, locale));
            }
        }
    }

    private static Set<String> keysOf(String resourcePath) throws IOException {
        Properties properties = new Properties();
        try (var reader = new InputStreamReader(new ClassPathResource(resourcePath).getInputStream(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties.stringPropertyNames();
    }

    @Nested
    class 에러코드 {

        @Test
        void 모든_에러코드는_ko_en_메시지를_가진다() {
            for (ErrorCode errorCode : ErrorCode.values()) {
                assertKeyExistsInAllLocales("error." + errorCode.name());
            }
        }
    }

    @Nested
    class 취향_요소_라벨 {

        @Test
        void 모든_취향_요소는_ko_en_min_max_라벨을_가진다() {
            for (PropensityFactor factor : PropensityFactor.values()) {
                assertKeyExistsInAllLocales("enum.PropensityFactor." + factor.name() + ".min");
                assertKeyExistsInAllLocales("enum.PropensityFactor." + factor.name() + ".max");
            }
        }
    }

    @Nested
    class 메일 {

        @Test
        void 메일_제목과_발신자명은_ko_en_메시지를_가진다() {
            assertKeyExistsInAllLocales("mail.fromName");
            assertKeyExistsInAllLocales("mail.emailVerification.subject");
            assertKeyExistsInAllLocales("mail.passwordReset.subject");
        }
    }

    @Nested
    class 키_집합 {

        @Test
        void ko와_en의_키_집합은_동일하다() throws IOException {
            Set<String> ko = keysOf("messages/messages.properties");
            Set<String> en = keysOf("messages/messages_en.properties");

            assertThat(en).containsExactlyInAnyOrderElementsOf(ko);
        }
    }
}
