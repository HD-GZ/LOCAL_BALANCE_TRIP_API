package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.domain.tourism.repository.dto.RegionVisitorSum;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@ExtendWith(MockitoExtension.class)
class RegionMetricsFinderTest {

    private static final LocalDate LATEST_BASE_DATE = LocalDate.of(2026, 7, 10);

    @Mock
    private TourRegionStatsRepository tourRegionStatsRepository;

    @Mock
    private RegionVisitorStatsRepository regionVisitorStatsRepository;

    @Mock
    private TourRegionStats tourRegionStats;

    @InjectMocks
    private RegionMetricsFinder regionMetricsFinder;

    @Test
    void 지역_통계와_최근_30일_외지인_방문자_합을_묶어_조회한다() {
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        통계_조회됨(regionCandidate);
        when(regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc())
            .thenReturn(Optional.of(방문자_통계(regionCandidate)));
        when(regionVisitorStatsRepository.sumByRegionCandidate(
            VisitorType.OUTSIDER, LATEST_BASE_DATE.minusDays(30)))
            .thenReturn(List.of(방문자_합(RegionCandidateFixture.CANDIDATE_ID, 1234.5)));

        List<RegionMetrics> result = regionMetricsFinder.findAllMetrics();

        assertThat(result).singleElement().satisfies(regionMetrics -> {
            assertThat(regionMetrics.regionName()).isEqualTo(RegionCandidateFixture.NAME);
            assertThat(regionMetrics.regionCandidateId()).isEqualTo(RegionCandidateFixture.CANDIDATE_ID);
            assertThat(regionMetrics.totalCount()).isEqualTo(10);
            assertThat(regionMetrics.recentOutsiderVisitors()).isEqualTo(1234.5);
        });
        verify(regionVisitorStatsRepository)
            .sumByRegionCandidate(VisitorType.OUTSIDER, LATEST_BASE_DATE.minusDays(30));
    }

    @Test
    void 방문자_집계에_없는_지역은_방문자_합을_0으로_채운다() {
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        통계_조회됨(regionCandidate);
        when(regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc())
            .thenReturn(Optional.of(방문자_통계(regionCandidate)));
        when(regionVisitorStatsRepository.sumByRegionCandidate(
            VisitorType.OUTSIDER, LATEST_BASE_DATE.minusDays(30)))
            .thenReturn(List.of(방문자_합(RegionCandidateFixture.CANDIDATE_ID + 1, 999.0)));

        List<RegionMetrics> result = regionMetricsFinder.findAllMetrics();

        assertThat(result).singleElement()
            .satisfies(regionMetrics -> assertThat(regionMetrics.recentOutsiderVisitors()).isZero());
    }

    @Test
    void 방문자_데이터가_전혀_없으면_모든_지역의_방문자_합이_0이다() {
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        통계_조회됨(regionCandidate);
        when(regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc()).thenReturn(Optional.empty());

        List<RegionMetrics> result = regionMetricsFinder.findAllMetrics();

        assertThat(result).singleElement()
            .satisfies(regionMetrics -> assertThat(regionMetrics.recentOutsiderVisitors()).isZero());
    }

    @Test
    void 관광_통계가_없으면_빈_목록을_반환한다() {
        when(tourRegionStatsRepository.findAllWithRegionCandidate()).thenReturn(List.of());
        when(regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc()).thenReturn(Optional.empty());

        List<RegionMetrics> result = regionMetricsFinder.findAllMetrics();

        assertThat(result).isEmpty();
    }

    private void 통계_조회됨(RegionCandidate regionCandidate) {
        when(tourRegionStatsRepository.findAllWithRegionCandidate()).thenReturn(List.of(tourRegionStats));
        when(tourRegionStats.getRegionCandidate()).thenReturn(regionCandidate);
        when(tourRegionStats.getTotalCount()).thenReturn(10);
        when(tourRegionStats.getSampleSize()).thenReturn(5);
        when(tourRegionStats.toTypeCounts()).thenReturn(Map.of());
    }

    private RegionVisitorStats 방문자_통계(RegionCandidate regionCandidate) {
        return RegionVisitorStats.create(regionCandidate, LATEST_BASE_DATE, VisitorType.OUTSIDER, 1000.0);
    }

    private RegionVisitorSum 방문자_합(Long regionCandidateId, double total) {
        return new RegionVisitorSum() {

            @Override
            public Long getRegionCandidateId() {
                return regionCandidateId;
            }

            @Override
            public double getTotal() {
                return total;
            }
        };
    }
}
