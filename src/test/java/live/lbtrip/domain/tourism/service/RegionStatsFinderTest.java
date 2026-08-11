package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;

@ExtendWith(MockitoExtension.class)
class RegionStatsFinderTest {

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @Mock
    private TourRegionStatsRepository tourRegionStatsRepository;

    @Mock
    private RegionCandidate regionCandidate;

    @Mock
    private TourRegionStats tourRegionStats;

    @InjectMocks
    private RegionStatsFinder regionStatsFinder;

    @Test
    void 지역_후보에_해당하는_관광_통계를_조회한다() {
        when(regionCandidateRepository.findAll()).thenReturn(List.of(regionCandidate));
        when(regionCandidate.getLdongRegnCd()).thenReturn("46");
        when(regionCandidate.getLdongSignguCd()).thenReturn("710");
        when(regionCandidate.getName()).thenReturn("담양");
        when(tourRegionStatsRepository.findByLdongRegnCdAndLdongSignguCd("46", "710"))
            .thenReturn(Optional.of(tourRegionStats));
        when(tourRegionStats.getLdongRegnCd()).thenReturn("46");
        when(tourRegionStats.getLdongSignguCd()).thenReturn("710");
        when(tourRegionStats.getTotalCount()).thenReturn(10);
        when(tourRegionStats.getSampleSize()).thenReturn(5);
        when(tourRegionStats.toTypeCounts()).thenReturn(Map.of());

        List<RegionStats> result = regionStatsFinder.findAll();

        assertThat(result).singleElement().satisfies(stats -> {
            assertThat(stats.regionName()).isEqualTo("담양");
            assertThat(stats.ldongRegnCd()).isEqualTo("46");
            assertThat(stats.ldongSignguCd()).isEqualTo("710");
            assertThat(stats.totalCount()).isEqualTo(10);
            assertThat(stats.sampleSize()).isEqualTo(5);
        });
    }

    @Test
    void 관광_통계가_없는_지역_후보는_제외한다() {
        when(regionCandidateRepository.findAll()).thenReturn(List.of(regionCandidate));
        when(regionCandidate.getLdongRegnCd()).thenReturn("46");
        when(regionCandidate.getLdongSignguCd()).thenReturn("710");
        when(tourRegionStatsRepository.findByLdongRegnCdAndLdongSignguCd("46", "710"))
            .thenReturn(Optional.empty());

        List<RegionStats> result = regionStatsFinder.findAll();

        assertThat(result).isEmpty();
    }
}
