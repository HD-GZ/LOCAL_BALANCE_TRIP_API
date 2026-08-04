package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;

import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.RecommendationFixture;

@ExtendWith(MockitoExtension.class)
class CourseComposerTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private CourseComposer courseComposer;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        courseComposer = new CourseComposer(
            chatClientBuilder,
            new ByteArrayResource("{regionName} {candidateLines} {maxCourses} {locality} {frugality} "
                .concat("{experientiality} {vitality} {sociality} {accommodation} {food} ")
                .concat("{experience} {transportation} {cafeExhibition}")
                .getBytes(StandardCharsets.UTF_8))
        );
    }

    @Nested
    class 구성 {

        @Test
        void 후보에_없는_ID와_중복_ID를_제거한다() {
            mockResponse(CourseComposition.of("추천 이유", List.of(
                CoursePlan.of("코스", "코스 이유", List.of("100", "100", "999", "200", "300")))));

            CourseComposition result = courseComposer.compose(
                PropensityFixture.propensity(), RecommendationFixture.REGION_NAME,
                RecommendationFixture.tourPlaces());

            assertThat(result.courses()).singleElement().satisfies(course ->
                assertThat(course.placeContentIds()).containsExactly("100", "200", "300"));
        }

        @Test
        void 코스는_최대_3개만_유지한다() {
            List<CoursePlan> courses = List.of(
                plan("코스1"), plan("코스2"), plan("코스3"), plan("코스4"));
            mockResponse(CourseComposition.of("추천 이유", courses));

            CourseComposition result = courseComposer.compose(
                PropensityFixture.propensity(), RecommendationFixture.REGION_NAME,
                RecommendationFixture.tourPlaces());

            assertThat(result.courses()).extracting(CoursePlan::name)
                .containsExactly("코스1", "코스2", "코스3");
        }

        @Test
        void LLM_호출에_실패하면_추천_생성_예외를_던진다() {
            when(chatClient.prompt()).thenThrow(new RuntimeException("LLM failed"));

            assertThatThrownBy(() -> courseComposer.compose(
                PropensityFixture.propensity(), RecommendationFixture.REGION_NAME,
                RecommendationFixture.tourPlaces()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        @Test
        void 유효한_코스가_없으면_추천_생성_예외를_던진다() {
            mockResponse(CourseComposition.of("추천 이유", List.of(
                CoursePlan.of("코스", "코스 이유", List.of("999")))));

            assertThatThrownBy(() -> courseComposer.compose(
                PropensityFixture.propensity(), RecommendationFixture.REGION_NAME,
                RecommendationFixture.tourPlaces()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
    }

    private CoursePlan plan(String name) {
        return CoursePlan.of(name, "코스 이유", List.of("100", "200", "300"));
    }

    private void mockResponse(CourseComposition response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(CourseComposition.class)).thenReturn(response);
    }
}
