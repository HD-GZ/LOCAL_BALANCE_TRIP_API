package live.lbtrip.domain.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class LocalizedMailTemplate {

    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";
    private static final String BASE_PATH = "classpath:templates/email/";

    private final Locale defaultLocale;
    private final Map<String, String> plainTextByLanguage;
    private final Map<String, String> htmlByLanguage;

    public LocalizedMailTemplate(
        String templateName,
        List<Locale> supportedLocales,
        Locale defaultLocale,
        ResourceLoader resourceLoader
    ) {
        this.defaultLocale = defaultLocale;
        this.plainTextByLanguage = load(templateName, supportedLocales, "txt", resourceLoader);
        this.htmlByLanguage = load(templateName, supportedLocales, "html", resourceLoader);
    }

    public String plainText(Locale locale, String code) {
        return select(plainTextByLanguage, locale).replace(CODE_PLACEHOLDER, code);
    }

    public String html(Locale locale, String code) {
        return select(htmlByLanguage, locale).replace(CODE_PLACEHOLDER, code);
    }

    private String select(Map<String, String> byLanguage, Locale locale) {
        return byLanguage.getOrDefault(locale.getLanguage(), byLanguage.get(defaultLocale.getLanguage()));
    }

    private static Map<String, String> load(
        String templateName,
        List<Locale> locales,
        String extension,
        ResourceLoader resourceLoader
    ) {
        return locales.stream().collect(Collectors.toMap(
            Locale::getLanguage,
            locale -> read(resourceLoader.getResource(
                BASE_PATH + templateName + "_" + locale.getLanguage() + "." + extension)),
            (a, b) -> a
        ));
    }

    private static String read(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("mail template not found: " + resource.getDescription(), exception);
        }
    }
}
