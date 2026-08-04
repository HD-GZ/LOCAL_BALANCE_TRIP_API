package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.recommendation.model.vo.CourseCandidateGroup;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RecommendationFixture;

class CourseCompositionValidatorTest {

    private final CourseCompositionValidator validator = new CourseCompositionValidator();

    @Nested
    class 정규화 {

        @Test
        void 텍스트를_정리하고_코스명에_지역명을_붙인다() {
            CourseComposition raw = CourseComposition.of("  지역 추천 이유  ", List.of(
                CoursePlan.of("  산책 코스  ", "  코스 추천 이유  ", ids("100", "200", "300"))));

            CourseComposition result = validator.validate(
                raw, RecommendationFixture.tourPlaces(), RecommendationFixture.REGION_NAME);

            assertThat(result.regionReason()).isEqualTo("지역 추천 이유");
            assertThat(result.courses()).singleElement().satisfies(course -> {
                assertThat(course.name()).isEqualTo(RecommendationFixture.REGION_NAME + " 산책 코스");
                assertThat(course.reason()).isEqualTo("코스 추천 이유");
            });
        }

        @Test
        void 유효한_장소를_중복_없이_최대_5개까지_유지한다() {
            CourseComposition raw = CourseComposition.of("지역 추천 이유", List.of(
                CoursePlan.of("코스", "이유", ids(" 100 ", "100", "999", "200", "300"))));

            CourseComposition result = validator.validate(
                raw, RecommendationFixture.tourPlaces(), RecommendationFixture.REGION_NAME);

            assertThat(result.courses().getFirst().placeContentIds())
                .containsExactly("100", "200", "300");
        }

        @Test
        void 최대_3개_코스만_유지한다() {
            CoursePlan course = CoursePlan.of("코스", "이유", ids("100", "200", "300"));

            CourseComposition result = validator.validate(
                CourseComposition.of("지역 추천 이유", List.of(course, course, course, course)),
                RecommendationFixture.tourPlaces(), RecommendationFixture.REGION_NAME);

            assertThat(result.courses()).hasSize(3);
        }

        @Test
        void 텍스트와_장소_수를_저장_제약에_맞게_제한한다() {
            String longText = "가".repeat(350);
            List<TourPlace> candidates = new ArrayList<>(RecommendationFixture.tourPlaces());
            candidates.add(place("400"));
            candidates.add(place("500"));
            candidates.add(place("600"));
            CourseComposition raw = CourseComposition.of(longText, List.of(
                CoursePlan.of("코스", longText, ids("100", "200", "300", "400", "500", "600"))));

            CourseComposition result = validator.validate(raw, candidates, RecommendationFixture.REGION_NAME);

            assertThat(result.regionReason()).hasSize(300);
            assertThat(result.courses().getFirst().reason()).hasSize(300);
            assertThat(result.courses().getFirst().placeContentIds()).hasSize(5);
        }

        @Test
        void 후보군별로_유효한_코스를_하나만_유지한다() {
            List<CourseCandidateGroup> groups = List.of(
                CourseCandidateGroup.of("G1", RecommendationFixture.tourPlaces()));
            CourseComposition raw = CourseComposition.of("지역 추천 이유", List.of(
                CoursePlan.of("G9", "없는 후보군", "이유", ids("100", "200", "300")),
                CoursePlan.of("G1", "장소 부족", "이유", ids("100", "200")),
                CoursePlan.of("G1", "유효 코스", "이유", ids("100", "200", "300")),
                CoursePlan.of("G1", "중복 코스", "이유", ids("100", "200", "300"))));

            CourseComposition result = validator.validateGrouped(
                raw, groups, RecommendationFixture.REGION_NAME);

            assertThat(result.courses()).singleElement().satisfies(course -> {
                assertThat(course.candidateGroupId()).isEqualTo("G1");
                assertThat(course.name()).isEqualTo(RecommendationFixture.REGION_NAME + " 유효 코스");
            });
        }
    }

    @Nested
    class 검증_실패 {

        @Test
        void 지역_추천_이유가_비어_있으면_예외를_던진다() {
            CourseComposition raw = CourseComposition.of(" ", List.of(
                CoursePlan.of("코스", "이유", ids("100", "200", "300"))));

            assertGenerationFailed(raw);
        }

        @Test
        void 장소가_3개_미만인_코스는_제거한다() {
            CourseComposition raw = CourseComposition.of("지역 추천 이유", List.of(
                CoursePlan.of("탈락 코스", "이유", ids("100", "200")),
                CoursePlan.of("유효 코스", "이유", ids("100", "200", "300"))));

            CourseComposition result = validator.validate(
                raw, RecommendationFixture.tourPlaces(), RecommendationFixture.REGION_NAME);

            assertThat(result.courses()).singleElement()
                .extracting(CoursePlan::name)
                .isEqualTo(RecommendationFixture.REGION_NAME + " 유효 코스");
        }

        @Test
        void 코스명이나_추천_이유가_비어_있으면_코스를_제거한다() {
            List<CoursePlan> courses = new ArrayList<>();
            courses.add(CoursePlan.of(null, "이유", ids("100", "200", "300")));
            courses.add(CoursePlan.of("코스", " ", ids("100", "200", "300")));

            assertGenerationFailed(CourseComposition.of("지역 추천 이유", courses));
        }

        @Test
        void 응답이나_코스가_없으면_예외를_던진다() {
            assertThatThrownBy(() -> validator.validate(
                null, RecommendationFixture.tourPlaces(), RecommendationFixture.REGION_NAME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        private void assertGenerationFailed(CourseComposition raw) {
            assertThatThrownBy(() -> validator.validate(
                raw, RecommendationFixture.tourPlaces(), RecommendationFixture.REGION_NAME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
    }

    private List<String> ids(String... values) {
        return List.of(values);
    }

    private TourPlace place(String contentId) {
        return TourPlace.create(
            contentId,
            RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD,
            12,
            "장소 " + contentId,
            RecommendationFixture.IMAGE_URL,
            127.0,
            35.0,
            Integer.parseInt(contentId)
        );
    }
}
