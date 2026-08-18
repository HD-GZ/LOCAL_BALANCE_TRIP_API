package live.lbtrip.support.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import live.lbtrip.global.i18n.LocaleConfig;
import live.lbtrip.global.i18n.MessageResolver;

@TestConfiguration
@Import({LocaleConfig.class, MessageResolver.class})
public class I18nTestConfig {
}
