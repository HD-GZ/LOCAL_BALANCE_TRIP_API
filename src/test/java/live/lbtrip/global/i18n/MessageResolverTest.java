package live.lbtrip.global.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import live.lbtrip.global.error.ErrorCode;

class MessageResolverTest {

    private final MessageResolver messageResolver = new MessageResolver(messageSource());

    private static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Nested
    class 현재_로케일_해석 {

        @Test
        void 로케일_컨텍스트가_없으면_한국어로_해석한다() {
            assertThat(messageResolver.currentLocale()).isEqualTo(Locale.KOREAN);
            assertThat(messageResolver.resolve("test.hello", "홍길동")).isEqualTo("안녕하세요 홍길동");
        }

        @Test
        void 영어_로케일이면_영어로_해석한다() {
            LocaleContextHolder.setLocale(Locale.ENGLISH);

            assertThat(messageResolver.resolve("test.hello", "Kim")).isEqualTo("Hello Kim");
        }

        @Test
        void 미지원_로케일이면_한국어로_폴백한다() {
            LocaleContextHolder.setLocale(Locale.JAPANESE);

            assertThat(messageResolver.resolve("test.hello", "홍길동")).isEqualTo("안녕하세요 홍길동");
        }
    }

    @Nested
    class 키_누락 {

        @Test
        void 키가_없으면_예외_없이_키_문자열을_반환한다() {
            LocaleContextHolder.setLocale(Locale.ENGLISH);

            assertThat(messageResolver.resolve("no.such.key")).isEqualTo("no.such.key");
        }
    }

    @Nested
    class 키_규칙 {

        @Test
        void 에러코드는_error_접두사_키로_해석한다() {
            assertThat(messageResolver.resolve(ErrorCode.USER_NOT_FOUND)).isEqualTo("error.USER_NOT_FOUND");
        }

    }
}
