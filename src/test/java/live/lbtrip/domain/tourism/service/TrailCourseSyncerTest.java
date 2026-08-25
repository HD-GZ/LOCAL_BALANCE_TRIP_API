package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.model.RegionGreenMetrics;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.region.repository.RegionGreenMetricsRepository;
import live.lbtrip.domain.tourism.client.DurunubiClient;
import live.lbtrip.domain.tourism.client.dto.DurunubiCourseItem;
import live.lbtrip.domain.tourism.model.entity.TrailCourse;
import live.lbtrip.domain.tourism.repository.TrailCourseRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TrailCourseFixture;

@ExtendWith(MockitoExtension.class)
class TrailCourseSyncerTest {

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @Mock
    private DurunubiClient durunubiClient;

    @Mock
    private TrailCourseRepository trailCourseRepository;

    @Mock
    private RegionGreenMetricsRepository regionGreenMetricsRepository;

    @InjectMocks
    private TrailCourseSyncer trailCourseSyncer;

    @Nested
    class 코스_적재 {

        @Test
        void 새_코스는_지역을_매칭해_저장하고_없는_지역은_null로_저장한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(candidate));
            when(durunubiClient.fetchCourses(1)).thenReturn(List.of(
                TrailCourseFixture.item("A", "전라남도 담양군"),
                TrailCourseFixture.item("B", "경기도 가평군")));
            when(trailCourseRepository.findByCrsIdx(any())).thenReturn(Optional.empty());
            when(trailCourseRepository.findRegionCandidateIdsWithCourses()).thenReturn(List.of());
            when(regionGreenMetricsRepository.findAllWithRegionCandidate()).thenReturn(List.of());

            trailCourseSyncer.sync();

            ArgumentCaptor<TrailCourse> captor = ArgumentCaptor.forClass(TrailCourse.class);
            verify(trailCourseRepository, org.mockito.Mockito.times(2)).save(captor.capture());
            assertThat(captor.getAllValues())
                .extracting(TrailCourse::getCrsIdx, TrailCourse::getRegionCandidate)
                .containsExactly(
                    org.assertj.core.groups.Tuple.tuple("A", candidate),
                    org.assertj.core.groups.Tuple.tuple("B", null));
        }

        @Test
        void 이미_있는_코스는_갱신한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            TrailCourse existing = TrailCourse.create(TrailCourseFixture.item(), null);
            DurunubiCourseItem changed = new DurunubiCourseItem(TrailCourseFixture.CRS_IDX, "새 이름",
                null, null, null, "전라남도 담양군", null, null, null, null);
            when(regionCandidateRepository.findAll()).thenReturn(List.of(candidate));
            when(durunubiClient.fetchCourses(1)).thenReturn(List.of(changed));
            when(trailCourseRepository.findByCrsIdx(TrailCourseFixture.CRS_IDX)).thenReturn(Optional.of(existing));
            when(trailCourseRepository.findRegionCandidateIdsWithCourses()).thenReturn(List.of());
            when(regionGreenMetricsRepository.findAllWithRegionCandidate()).thenReturn(List.of());

            trailCourseSyncer.sync();

            verify(trailCourseRepository).save(existing);
            assertThat(existing.getName()).isEqualTo("새 이름");
            assertThat(existing.getRegionCandidate()).isEqualTo(candidate);
        }

        @Test
        void 페이지가_가득_차면_다음_페이지를_계속_조회한다() {
            when(regionCandidateRepository.findAll()).thenReturn(List.of());
            List<DurunubiCourseItem> fullPage = java.util.stream.IntStream.range(0, DurunubiClient.PAGE_SIZE)
                .mapToObj(i -> TrailCourseFixture.item("C" + i, null))
                .toList();
            when(durunubiClient.fetchCourses(1)).thenReturn(fullPage);
            when(durunubiClient.fetchCourses(2)).thenReturn(List.of(TrailCourseFixture.item("LAST", null)));
            when(trailCourseRepository.findByCrsIdx(any())).thenReturn(Optional.empty());
            when(trailCourseRepository.findRegionCandidateIdsWithCourses()).thenReturn(List.of());
            when(regionGreenMetricsRepository.findAllWithRegionCandidate()).thenReturn(List.of());

            trailCourseSyncer.sync();

            verify(durunubiClient).fetchCourses(2);
            verify(durunubiClient, never()).fetchCourses(3);
            verify(trailCourseRepository, org.mockito.Mockito.times(DurunubiClient.PAGE_SIZE + 1)).save(any());
        }

        @Test
        void 한도_초과_시_적재를_중단하고_gpx_인접_여부는_갱신한다() {
            when(regionCandidateRepository.findAll()).thenReturn(List.of());
            when(durunubiClient.fetchCourses(1)).thenThrow(BusinessException.of(ErrorCode.TOUR_API_QUOTA_EXCEEDED));
            when(trailCourseRepository.findRegionCandidateIdsWithCourses()).thenReturn(List.of());
            when(regionGreenMetricsRepository.findAllWithRegionCandidate()).thenReturn(List.of());

            trailCourseSyncer.sync();

            verify(trailCourseRepository, never()).save(any());
            verify(regionGreenMetricsRepository).findAllWithRegionCandidate();
        }
    }

    @Nested
    class gpx_인접_갱신 {

        @Test
        void 코스가_있는_지역만_gpx_인접으로_표시하고_기존_행만_갱신한다() {
            RegionCandidate withTrail = RegionCandidateFixture.candidateWithId();
            RegionCandidate withoutTrail = RegionCandidate.create("경상남도 고성군", "48", "820");
            org.springframework.test.util.ReflectionTestUtils.setField(withoutTrail, "id", 2L);
            RegionGreenMetrics adjacent = RegionGreenMetrics.create(withTrail, false, true, true, true, true);
            RegionGreenMetrics notAdjacent = RegionGreenMetrics.create(withoutTrail, true, true, true, true, true);
            when(regionCandidateRepository.findAll()).thenReturn(List.of(withTrail, withoutTrail));
            when(durunubiClient.fetchCourses(1)).thenReturn(List.of());
            when(trailCourseRepository.findRegionCandidateIdsWithCourses()).thenReturn(List.of(withTrail.getId()));
            when(regionGreenMetricsRepository.findAllWithRegionCandidate()).thenReturn(List.of(adjacent, notAdjacent));

            trailCourseSyncer.sync();

            assertThat(adjacent.isGpxAdjacent()).isTrue();
            assertThat(notAdjacent.isGpxAdjacent()).isFalse();
            verify(regionGreenMetricsRepository).saveAll(List.of(adjacent, notAdjacent));
        }
    }
}
