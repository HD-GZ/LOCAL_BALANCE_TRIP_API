package live.lbtrip.domain.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.UserFixture;

@DataJpaTest
class RecommendationOrderRepositoryTest {

    @Autowired
    private RecommendedRegionRepository recommendedRegionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 코스와_장소를_표시_순서대로_조회한다() {
        User user = userRepository.save(UserFixture.user());
        RecommendedRegion region = RecommendedRegion.create(
            user,
            RecommendationFixture.REGION_NAME,
            RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD,
            RecommendationFixture.IMAGE_URL,
            RecommendationFixture.REGION_REASON,
            1
        );
        region.addCourse(course(user, "두 번째 코스", 2));
        region.addCourse(course(user, "첫 코스", 1));
        recommendedRegionRepository.save(region);
        entityManager.flush();
        entityManager.clear();

        RecommendedRegion found = recommendedRegionRepository
            .findByIdAndUserId(region.getId(), user.getId())
            .orElseThrow();

        assertThat(found.getCourses()).extracting(GeneratedCourse::getName)
            .containsExactly("첫 코스", "두 번째 코스");
        assertThat(found.getCourses().getFirst().getPlaces()).extracting(CoursePlace::getVisitOrder)
            .containsExactly(1, 2);
    }

    private GeneratedCourse course(User user, String name, int displayOrder) {
        GeneratedCourse course = GeneratedCourse.create(
            user, name, RecommendationFixture.COURSE_REASON,
            RecommendationFixture.IMAGE_URL, displayOrder);
        course.addPlace(place(2));
        course.addPlace(place(1));
        return course;
    }

    private CoursePlace place(int visitOrder) {
        return CoursePlace.create(
            visitOrder, "장소 " + visitOrder, null, RecommendationFixture.IMAGE_URL,
            35.0, 127.0, visitOrder == 1 ? null : 10, false, null);
    }
}
