package live.lbtrip.support.fixture;

import java.math.BigDecimal;

import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.dto.DurunubiCourseItem;
import live.lbtrip.domain.tourism.model.entity.TrailCourse;

public final class TrailCourseFixture {

    public static final long TRAIL_COURSE_ID = 1L;
    public static final String CRS_IDX = "T_CRS_MNG0000000123";
    public static final String NAME = "담양 메타세쿼이아길";
    public static final BigDecimal DISTANCE_KM = new BigDecimal("12.50");
    public static final int REQUIRED_MINUTES = 240;
    public static final int LEVEL = 2;
    public static final String SIGUN = "전라남도 담양군";
    public static final String GPX_PATH = "https://example.com/a.gpx";
    public static final String BRD_DIV = "DNWW";
    public static final String ROUTE_IDX = "T_ROUTE_MNG0000000001";
    public static final String SUMMARY = "메타세쿼이아 가로수길을 따라 걷는 코스";

    private TrailCourseFixture() {
    }

    public static DurunubiCourseItem item() {
        return item(CRS_IDX, SIGUN);
    }

    public static DurunubiCourseItem item(String crsIdx, String sigun) {
        return new DurunubiCourseItem(crsIdx, NAME, DISTANCE_KM, REQUIRED_MINUTES, LEVEL, sigun,
            GPX_PATH, BRD_DIV, ROUTE_IDX, SUMMARY);
    }

    public static TrailCourse trailCourse() {
        return TrailCourse.create(item(), RegionCandidateFixture.candidateWithId());
    }

    public static TrailCourse trailCourseWithId() {
        TrailCourse course = trailCourse();
        ReflectionTestUtils.setField(course, "id", TRAIL_COURSE_ID);
        return course;
    }

    public static TrailCourse trailCourse(RegionCandidate candidate) {
        return TrailCourse.create(item(), candidate);
    }
}
