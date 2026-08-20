package live.lbtrip.global.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageResolver {

    private static final String ERROR_PREFIX = "error.";
    private final MessageSource messageSource;

    public Locale currentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        log.info("current locale: {}", locale);
        return LocaleConfig.SUPPORTED_LOCALES.stream()
            .filter(supported -> supported.getLanguage().equals(locale.getLanguage()))
            .findFirst()
            .orElse(LocaleConfig.DEFAULT_LOCALE);
    }

    public String resolve(String key, Object... args) {
        return resolve(currentLocale(), key, args);
    }

    public String resolve(Locale locale, String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException primaryMiss) {
            if (!LocaleConfig.DEFAULT_LOCALE.equals(locale)) {
                try {
                    return messageSource.getMessage(key, args, LocaleConfig.DEFAULT_LOCALE);
                } catch (NoSuchMessageException fallbackMiss) {
                    log.warn("Missing message key: {} (locale={}, fallback={})", key, locale, LocaleConfig.DEFAULT_LOCALE);
                    return key;
                }
            }
            log.warn("Missing message key: {} (locale={})", key, locale);
            return key;
        }
    }

    public String resolve(ErrorCode errorCode) {
        return resolve(ERROR_PREFIX + errorCode.name());
    }
}
