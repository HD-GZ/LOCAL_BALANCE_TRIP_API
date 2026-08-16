package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class LocalizedMailTemplateTest {

    private final LocalizedMailTemplate template = new LocalizedMailTemplate(
        "email-verification",
        List.of(Locale.KOREAN, Locale.ENGLISH),
        Locale.KOREAN,
        new DefaultResourceLoader()
    );

    @Nested
    class 로케일별_템플릿 {

        @Test
        void 한국어_로케일이면_한국어_템플릿에_코드를_치환한다() {
            String text = template.plainText(Locale.KOREAN, "123456");

            assertThat(text).contains("인증번호: 123456").doesNotContain("{{verificationCode}}");
        }

        @Test
        void 영어_로케일이면_영어_템플릿에_코드를_치환한다() {
            String html = template.html(Locale.ENGLISH, "123456");

            assertThat(html).contains("123456").contains("<html lang=\"en\">").doesNotContain("{{verificationCode}}");
        }

        @Test
        void 미지원_로케일이면_기본_로케일_템플릿을_사용한다() {
            String text = template.plainText(Locale.JAPANESE, "123456");

            assertThat(text).contains("인증번호: 123456");
        }
    }
}
