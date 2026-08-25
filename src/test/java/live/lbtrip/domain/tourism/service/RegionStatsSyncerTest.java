package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
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
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.AreaBasedItem;
import live.lbtrip.domain.tourism.client.dto.AreaBasedSample;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@ExtendWith(MockitoExtension.class)
class RegionStatsSyncerTest {

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private TourRegionStatsRepository tourRegionStatsRepository;

    @InjectMocks
    private RegionStatsSyncer regionStatsSyncer;

    @Nested
    class 지역_통계_집계 {

        @Test
        void 축제_항목은_표본_크기와_유형_집계에서_제외해_비율이_희석되지_않는다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            AreaBasedItem spot = new AreaBasedItem(TourContentType.TOURIST_SPOT.getCode(), "A01", "A0101", "A01010100");
            AreaBasedItem restaurant = new AreaBasedItem(TourContentType.RESTAURANT.getCode(), "A05", "A0502", "A05020100");
            AreaBasedItem festival = new AreaBasedItem(TourContentType.FESTIVAL.getCode(), "A02", "A0207", "A02070200");
            when(tourApiClient.fetchAreaBasedSample(candidate))
                .thenReturn(AreaBasedSample.of(4, List.of(spot, festival, restaurant, festival)))
                .thenReturn(AreaBasedSample.of(2, List.of(spot, restaurant)));
            when(tourRegionStatsRepository.findByRegionCandidateId(candidate.getId())).thenReturn(Optional.empty());

            regionStatsSyncer.sync(candidate);
            regionStatsSyncer.sync(candidate);

            ArgumentCaptor<TourRegionStats> captor = ArgumentCaptor.forClass(TourRegionStats.class);
            verify(tourRegionStatsRepository, times(2)).save(captor.capture());
            TourRegionStats withFestival = captor.getAllValues().get(0);
            TourRegionStats withoutFestival = captor.getAllValues().get(1);
            assertThat(withFestival.getSampleSize()).isEqualTo(2);
            assertThat(withFestival.getSampleSize()).isEqualTo(withoutFestival.getSampleSize());
            assertThat(withFestival.toTypeCounts()).isEqualTo(withoutFestival.toTypeCounts());
            assertThat(withFestival.toGroupCounts()).isEqualTo(withoutFestival.toGroupCounts());
        }
    }
}
