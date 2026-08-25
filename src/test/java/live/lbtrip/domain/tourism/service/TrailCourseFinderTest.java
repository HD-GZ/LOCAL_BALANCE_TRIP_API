package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import live.lbtrip.domain.tourism.model.entity.TrailCourse;
import live.lbtrip.domain.tourism.repository.TrailCourseRepository;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TrailCourseFixture;

@ExtendWith(MockitoExtension.class)
class TrailCourseFinderTest {

    @Mock
    private TrailCourseRepository trailCourseRepository;

    @InjectMocks
    private TrailCourseFinder trailCourseFinder;

    @Nested
    class 지역별_조회 {

        @Test
        void 거리순_상위_코스를_조회한다() {
            TrailCourse course = TrailCourseFixture.trailCourse();
            when(trailCourseRepository.findByRegionCandidateIdOrderByDistance(
                RegionCandidateFixture.CANDIDATE_ID, PageRequest.of(0, TrailCourseFinder.MAX_TRAILS)))
                .thenReturn(List.of(course));

            List<TrailCourse> trails = trailCourseFinder.findByRegion(RegionCandidateFixture.CANDIDATE_ID);

            assertThat(trails).containsExactly(course);
        }

        @Test
        void 지역이_없으면_빈_목록을_반환한다() {
            List<TrailCourse> trails = trailCourseFinder.findByRegion(null);

            assertThat(trails).isEmpty();
            verify(trailCourseRepository, never()).findByRegionCandidateIdOrderByDistance(any(), any());
        }
    }
}
