package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;

import live.lbtrip.domain.recommendation.model.vo.CourseCandidateGroup;
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
                .getBytes(StandardCharsets.UTF_8)),
            new CourseCompositionValidator()
        );
    }

    @Nested
    class 구성 {

        @Test
        void 후보에_없는_ID와_중복_ID를_제거한다() {
            mockResponse(CourseComposition.of("추천 이유", List.of(
                CoursePlan.of("G1", "코스", "코스 이유", List.of("100", "100", "999", "200", "300")))));

            CourseComposition result = compose();

            assertThat(result.courses()).singleElement().satisfies(course ->
                assertThat(course.placeContentIds()).containsExactly("100", "200", "300"));
        }

        @Test
        void LLM_호출에_실패하면_추천_생성_예외를_던진다() {
            when(chatClient.prompt()).thenThrow(new RuntimeException("LLM failed"));

            assertThatThrownBy(CourseComposerTest.this::compose)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        @Test
        void 후보군_ID와_좌표를_LLM에_전달한다() {
            List<CourseCandidateGroup> groups = List.of(
                CourseCandidateGroup.of("G1", RecommendationFixture.tourPlaces()));
            mockResponse(CourseComposition.of("추천 이유", List.of(
                CoursePlan.of("G1", "코스", "코스 이유", List.of("100", "200", "300")))));
            ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

            CourseComposition result = courseComposer.composeGrouped(
                PropensityFixture.propensity(), RecommendationFixture.REGION_NAME, groups);

            verify(requestSpec).user(promptCaptor.capture());
            assertThat(promptCaptor.getValue())
                .contains("G1 | 100 | 관광지 | 죽녹원 | 126.986 | 35.325");
            assertThat(result.courses().getFirst().candidateGroupId()).isEqualTo("G1");
        }

        @Test
        void 유효한_코스가_없으면_추천_생성_예외를_던진다() {
            mockResponse(CourseComposition.of("추천 이유", List.of(
                CoursePlan.of("G1", "코스", "코스 이유", List.of("999")))));

            assertThatThrownBy(CourseComposerTest.this::compose)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
    }

    private CourseComposition compose() {
        return courseComposer.composeGrouped(
            PropensityFixture.propensity(),
            RecommendationFixture.REGION_NAME,
            List.of(CourseCandidateGroup.of("G1", RecommendationFixture.tourPlaces()))
        );
    }

    private void mockResponse(CourseComposition response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(CourseComposition.class)).thenReturn(response);
    }
}
