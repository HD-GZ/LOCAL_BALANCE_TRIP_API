package live.lbtrip.support.fixture;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace;
import live.lbtrip.domain.savedcourse.share.model.entity.CourseShareToken;

public final class CourseShareFixture {

    public static final Long SHARE_TOKEN_ID = 1L;
    public static final Long SAVED_COURSE_ID = 3L;
    public static final String TOKEN = "550e8400-e29b-41d4-a716-446655440000";
    public static final LocalDateTime EXPIRES_AT = LocalDateTime.now().plusDays(7).withNano(0);

    private CourseShareFixture() {
    }

    public static SavedCourse savedCourse() {
        SavedCourse savedCourse = SavedCourse.create(
            UserFixture.user(),
            RecommendationFixture.COURSE_ID,
            RecommendationFixture.COURSE_NAME,
            RecommendationFixture.REGION_NAME,
            RecommendationFixture.COURSE_REASON,
            RecommendationFixture.IMAGE_URL,
            RegionCandidateFixture.candidateWithId()
        );
        ReflectionTestUtils.setField(savedCourse, "id", SAVED_COURSE_ID);
        return savedCourse;
    }

    public static SavedCourse savedCourseWithPlaces() {
        SavedCourse savedCourse = savedCourse();
        savedCourse.addPlace(SavedCoursePlace.create(
            1, "죽녹원", "대나무 숲", RecommendationFixture.IMAGE_URL,
            35.325, 126.986, null, false, null, RecommendationFixture.PLACE_REASON));
        savedCourse.addPlace(SavedCoursePlace.create(
            2, "관방제림", "천연기념물 숲길", RecommendationFixture.IMAGE_URL,
            35.321, 126.981, 10, false, null, null));
        return savedCourse;
    }

    public static SavedCourseDetailResponse savedCourseDetailResponse() {
        return SavedCourseDetailResponse.of(savedCourseWithPlaces(), List.of());
    }

    public static CourseShareToken shareToken() {
        CourseShareToken shareToken = CourseShareToken.create(savedCourse(), TOKEN, EXPIRES_AT);
        ReflectionTestUtils.setField(shareToken, "id", SHARE_TOKEN_ID);
        return shareToken;
    }
}
