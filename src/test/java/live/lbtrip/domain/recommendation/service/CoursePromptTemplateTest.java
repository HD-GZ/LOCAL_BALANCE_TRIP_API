package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;

class CoursePromptTemplateTest {

    private static final Map<String, Object> VARIABLES = Map.ofEntries(
        Map.entry("regionName", "담양군"),
        Map.entry("locality", 4),
        Map.entry("frugality", 4),
        Map.entry("experientiality", 3),
        Map.entry("vitality", 2),
        Map.entry("sociality", 1),
        Map.entry("accommodation", 2),
        Map.entry("food", 5),
        Map.entry("experience", 3),
        Map.entry("transportation", 2),
        Map.entry("cafeExhibition", 4),
        Map.entry("candidateLines", "100 | 관광지 | 죽녹원 | 126.986 | 35.325"),
        Map.entry("maxCourses", 3)
    );

    @Nested
    class 렌더링 {

        @Test
        void 한국어_템플릿이_변수를_치환해_렌더링된다() {
            String rendered = render("prompts/course-composition.st");

            assertThat(rendered)
                .contains("\"담양군\" 지역")
                .contains("죽녹원")
                .contains("최대 3개")
                .contains("contentId")
                .contains("reason")
                .doesNotContain("{");
        }

        @Test
        void 영문_템플릿이_변수를_치환해_렌더링된다() {
            String rendered = render("prompts/course-composition-en.st");

            assertThat(rendered)
                .contains("\"담양군\" area")
                .contains("죽녹원")
                .contains("at most 3 courses")
                .contains("contentId")
                .contains("reason")
                .doesNotContain("{");
        }
    }

    private String render(String path) {
        return new PromptTemplate(new ClassPathResource(path)).render(VARIABLES);
    }
}
