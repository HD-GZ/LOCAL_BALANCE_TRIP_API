package live.lbtrip.domain.tourism.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.dto.DurunubiCourseItem;
import live.lbtrip.domain.tourism.model.entity.TrailCourse;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TrailCourseFixture;

class TrailCourseTest {

    @Nested
    class 생성 {

        @Test
        void 두루누비_항목으로_생성한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();

            TrailCourse course = TrailCourse.create(TrailCourseFixture.item(), candidate);

            assertThat(course.getCrsIdx()).isEqualTo(TrailCourseFixture.CRS_IDX);
            assertThat(course.getName()).isEqualTo(TrailCourseFixture.NAME);
            assertThat(course.getDistanceKm()).isEqualByComparingTo(TrailCourseFixture.DISTANCE_KM);
            assertThat(course.getRequiredMinutes()).isEqualTo(TrailCourseFixture.REQUIRED_MINUTES);
            assertThat(course.getLevel()).isEqualTo(TrailCourseFixture.LEVEL);
            assertThat(course.getSigun()).isEqualTo(TrailCourseFixture.SIGUN);
            assertThat(course.getRegionCandidate()).isEqualTo(candidate);
        }
    }

    @Nested
    class 갱신 {

        @Test
        void 항목_값과_지역을_갱신한다() {
            TrailCourse course = TrailCourse.create(TrailCourseFixture.item(), null);
            DurunubiCourseItem changed = new DurunubiCourseItem(TrailCourseFixture.CRS_IDX, "새 이름",
                new BigDecimal("3.10"), 60, 1, "전라남도 담양군", "https://example.com/b.gpx",
                "DNBW", "R2", "요약");
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();

            course.update(changed, candidate);

            assertThat(course.getName()).isEqualTo("새 이름");
            assertThat(course.getDistanceKm()).isEqualByComparingTo("3.10");
            assertThat(course.getRequiredMinutes()).isEqualTo(60);
            assertThat(course.getLevel()).isEqualTo(1);
            assertThat(course.getSigun()).isEqualTo("전라남도 담양군");
            assertThat(course.getGpxPath()).isEqualTo("https://example.com/b.gpx");
            assertThat(course.getBrdDiv()).isEqualTo("DNBW");
            assertThat(course.getRouteIdx()).isEqualTo("R2");
            assertThat(course.getSummary()).isEqualTo("요약");
            assertThat(course.getRegionCandidate()).isEqualTo(candidate);
        }

        @Test
        void 지역을_지정한다() {
            TrailCourse course = TrailCourse.create(TrailCourseFixture.item(), null);
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();

            course.assignRegion(candidate);

            assertThat(course.getRegionCandidate()).isEqualTo(candidate);
        }
    }
}
