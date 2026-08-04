package live.lbtrip.support.fixture;

import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.user.model.User;

public final class RecommendationFixture {

    public static final long REGION_ID = 1L;
    public static final long COURSE_ID = 10L;
    public static final String REGION_NAME = "전라남도 담양군";
    public static final String REGION_REASON = "한적한 로컬 여행에 어울리는 지역이에요.";
    public static final String COURSE_NAME = "전라남도 담양군 산책 코스";
    public static final String COURSE_REASON = "자연과 문화를 함께 둘러보는 코스예요.";
    public static final String IMAGE_URL = "https://images.example.com/course.jpg";
    public static final String LDONG_REGN_CD = "46";
    public static final String LDONG_SIGNGU_CD = "710";

    private RecommendationFixture() {
    }

    public static RecommendedRegion region() {
        User user = UserFixture.user();
        RecommendedRegion region = RecommendedRegion.create(
            user, REGION_NAME, LDONG_REGN_CD, LDONG_SIGNGU_CD,
            IMAGE_URL, REGION_REASON, 1);
        ReflectionTestUtils.setField(region, "id", REGION_ID);
        region.addCourse(course(user));
        return region;
    }

    public static GeneratedCourse course() {
        return course(UserFixture.user());
    }

    private static GeneratedCourse course(User user) {
        GeneratedCourse course = GeneratedCourse.create(user, COURSE_NAME, COURSE_REASON, IMAGE_URL);
        ReflectionTestUtils.setField(course, "id", COURSE_ID);
        course.addPlace(CoursePlace.create(
            1, "죽녹원", "대나무 숲", IMAGE_URL,
            35.325, 126.986, null, false, null));
        course.addPlace(CoursePlace.create(
            2, "관방제림", "천연기념물 숲길", IMAGE_URL,
            35.321, 126.981, 10, true, "https://audio.example.com/guide.mp3"));
        return course;
    }

    public static List<TourPlace> tourPlaces() {
        return List.of(
            TourPlace.create("100", LDONG_REGN_CD, LDONG_SIGNGU_CD, 12,
                "죽녹원", IMAGE_URL, 126.986, 35.325, 1),
            TourPlace.create("200", LDONG_REGN_CD, LDONG_SIGNGU_CD, 14,
                "관방제림", IMAGE_URL, 126.981, 35.321, 2),
            TourPlace.create("300", LDONG_REGN_CD, LDONG_SIGNGU_CD, 39,
                "담양시장", IMAGE_URL, 126.979, 35.319, 3)
        );
    }

    public static RegionRecommendationResponse regionResponse() {
        return new RegionRecommendationResponse(REGION_ID, REGION_NAME, IMAGE_URL, REGION_REASON);
    }

    public static CourseCandidateResponse courseResponse() {
        return new CourseCandidateResponse(COURSE_ID, COURSE_NAME, IMAGE_URL, COURSE_REASON);
    }

    public static CourseDetailResponse courseDetailResponse() {
        return CourseDetailResponse.of(region().getCourses().getFirst(), List.of());
    }
}
