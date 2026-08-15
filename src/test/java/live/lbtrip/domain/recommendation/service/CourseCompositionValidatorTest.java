package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.recommendation.model.vo.WalkableCluster;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.config.RecommendationProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RecommendationFixture;

class CourseCompositionValidatorTest {

    private static final String REGION_NAME = RecommendationFixture.REGION_NAME;

    private final CourseCompositionValidator validator =
        new CourseCompositionValidator(new RecommendationProperties(3, 2, List.of(1500)));

    private final List<WalkableCluster> candidates = RecommendationFixture.walkableClusters();

    @Nested
    class 장소_검증 {

        @Test
        void 후보에_없는_ID와_중복_ID를_제거한다() {
            CourseComposition raw = composition(
                course("코스", Arrays.asList("100", "100", "999", null, " 200 ", "300")));

            CourseComposition result = validator.validate(raw, candidates, REGION_NAME);

            assertThat(result.courses()).singleElement().satisfies(course ->
                assertThat(course.placeContentIds()).containsExactly("100", "200", "300"));
        }

        @Test
        void 앞_코스가_사용한_장소는_뒤_코스에서_제거한다() {
            CourseComposition raw = composition(
                course("첫 코스", List.of("100", "200", "300")),
                course("둘째 코스", List.of("100", "200", "300")));

            CourseComposition result = validator.validate(raw, candidates, REGION_NAME);

            assertThat(result.courses()).hasSize(1);
        }

        @Test
        void 코스당_장소는_5곳까지만_담는다() {
            List<WalkableCluster> manyCandidates =
                RecommendationFixture.walkableClusters(RecommendationFixture.manyTourPlaces(7));
            CourseComposition raw = composition(
                course("코스", List.of("100", "101", "102", "103", "104", "105", "106")));

            CourseComposition result = validator.validate(raw, manyCandidates, REGION_NAME);

            assertThat(result.courses().getFirst().placeContentIds())
                .containsExactly("100", "101", "102", "103", "104");
        }

        @Test
        void 첫_유효_장소와_다른_클러스터의_장소는_제거한다() {
            List<TourPlace> places = RecommendationFixture.manyTourPlaces(6);
            List<WalkableCluster> clusters = List.of(
                WalkableCluster.of("1", places.subList(0, 3)),
                WalkableCluster.of("2", places.subList(3, 6)));
            CourseComposition raw = composition(
                course("코스", List.of("100", "103", "101", "104", "102")));

            CourseComposition result = validator.validate(raw, clusters, REGION_NAME);

            assertThat(result.courses()).singleElement().satisfies(course ->
                assertThat(course.placeContentIds()).containsExactly("100", "101", "102"));
        }

        @Test
        void 클러스터를_넘나들어_한_클러스터에_3곳이_안_남는_코스는_탈락한다() {
            List<TourPlace> places = RecommendationFixture.manyTourPlaces(6);
            List<WalkableCluster> clusters = List.of(
                WalkableCluster.of("1", places.subList(0, 3)),
                WalkableCluster.of("2", places.subList(3, 6)));
            CourseComposition raw = composition(
                course("흩어진 코스", List.of("100", "101", "103", "104")),
                course("정상 코스", List.of("103", "104", "105")));

            CourseComposition result = validator.validate(raw, clusters, REGION_NAME);

            assertThat(result.courses()).singleElement().satisfies(course ->
                assertThat(course.name()).contains("정상 코스"));
        }

        @Test
        void 유효_장소가_3곳_미만인_코스는_탈락한다() {
            CourseComposition raw = composition(
                course("빈약한 코스", List.of("100", "200")),
                course("정상 코스", List.of("100", "200", "300")));

            CourseComposition result = validator.validate(raw, candidates, REGION_NAME);

            assertThat(result.courses()).singleElement().satisfies(course ->
                assertThat(course.name()).contains("정상 코스"));
        }
    }

    @Nested
    class 코스_구조_검증 {

        @Test
        void 코스_수가_상한을_넘으면_초과분을_버린다() {
            List<WalkableCluster> manyCandidates =
                RecommendationFixture.walkableClusters(RecommendationFixture.manyTourPlaces(9));
            CourseComposition raw = composition(
                course("코스1", List.of("100", "101", "102")),
                course("코스2", List.of("103", "104", "105")),
                course("코스3", List.of("106", "107", "108")));

            CourseComposition result = validator.validate(raw, manyCandidates, REGION_NAME);

            assertThat(result.courses()).hasSize(2);
        }

        @Test
        void 이름이_지역명으로_시작하지_않으면_지역명을_붙인다() {
            CourseComposition raw = composition(course("골목 미식 코스", List.of("100", "200", "300")));

            CourseComposition result = validator.validate(raw, candidates, REGION_NAME);

            assertThat(result.courses().getFirst().name())
                .isEqualTo(REGION_NAME + " 골목 미식 코스");
        }

        @Test
        void 이름이나_이유가_없는_코스는_탈락한다() {
            CourseComposition raw = composition(
                CoursePlan.of(" ", "이유", List.of("100", "200", "300")),
                CoursePlan.of("이름", null, List.of("100", "200", "300")));

            assertThatThrownBy(() -> validator.validate(raw, candidates, REGION_NAME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
    }

    @Nested
    class 지역_수준_검증 {

        @Test
        void 지역_추천_이유가_없으면_예외를_던진다() {
            CourseComposition raw = CourseComposition.of(" ",
                List.of(course("코스", List.of("100", "200", "300"))));

            assertThatThrownBy(() -> validator.validate(raw, candidates, REGION_NAME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        @Test
        void 응답에_코스가_없으면_예외를_던진다() {
            assertThatThrownBy(() -> validator.validate(
                CourseComposition.of("이유", List.of()), candidates, REGION_NAME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        @Test
        void 모든_코스가_탈락하면_예외를_던진다() {
            CourseComposition raw = composition(course("환각 코스", List.of("998", "999")));

            assertThatThrownBy(() -> validator.validate(raw, candidates, REGION_NAME))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
    }

    private CourseComposition composition(CoursePlan... courses) {
        return CourseComposition.of("지역 추천 이유", List.of(courses));
    }

    private CoursePlan course(String name, List<String> placeContentIds) {
        return CoursePlan.of(name, "코스 이유", placeContentIds);
    }
}
