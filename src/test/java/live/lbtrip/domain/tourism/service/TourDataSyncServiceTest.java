package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.DataLabClient;
import live.lbtrip.domain.tourism.client.OdiiClient;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.VisitorStatItem;
import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@ExtendWith(MockitoExtension.class)
class TourDataSyncServiceTest {

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private OdiiClient odiiClient;

    @Mock
    private DataLabClient dataLabClient;

    @Mock
    private TourRegionStatsRepository tourRegionStatsRepository;

    @Mock
    private TourPlaceRepository tourPlaceRepository;

    @Mock
    private OdiiThemeRepository odiiThemeRepository;

    @Mock
    private RegionVisitorStatsRepository regionVisitorStatsRepository;

    @InjectMocks
    private TourDataSyncService tourDataSyncService;

    @Test
    void 후보_지역과_일치하는_방문자_행만_저장한다() {
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        when(regionCandidateRepository.findAll()).thenReturn(List.of(regionCandidate));
        when(tourPlaceRepository.findAllByOverviewIsNull()).thenReturn(List.of());
        when(odiiThemeRepository.findAllByAudioSyncedAtIsNull()).thenReturn(List.of());
        when(regionVisitorStatsRepository.existsByBaseDate(any())).thenReturn(false);
        when(regionVisitorStatsRepository
            .findByRegionCandidateIdAndBaseDateAndVisitorType(any(), any(), any()))
            .thenReturn(Optional.empty());
        LocalDate someDay = LocalDate.now().minusDays(10);
        when(dataLabClient.fetchDailyVisitors(any())).thenReturn(List.of());
        when(dataLabClient.fetchDailyVisitors(someDay)).thenReturn(List.of(
            new VisitorStatItem(
                RegionCandidateFixture.LDONG_REGN_CD + RegionCandidateFixture.LDONG_SIGNGU_CD,
                VisitorType.OUTSIDER, 500.0, someDay),
            new VisitorStatItem("11110", VisitorType.OUTSIDER, 999.0, someDay)));

        tourDataSyncService.syncAll();

        ArgumentCaptor<RegionVisitorStats> captor = ArgumentCaptor.forClass(RegionVisitorStats.class);
        verify(regionVisitorStatsRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
            .allSatisfy(saved -> assertThat(saved.getRegionCandidate()).isEqualTo(regionCandidate));
    }

    @Test
    void 이미_적재된_일자는_다시_조회하지_않는다() {
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        when(regionCandidateRepository.findAll()).thenReturn(List.of(regionCandidate));
        when(tourPlaceRepository.findAllByOverviewIsNull()).thenReturn(List.of());
        when(odiiThemeRepository.findAllByAudioSyncedAtIsNull()).thenReturn(List.of());
        when(regionVisitorStatsRepository.existsByBaseDate(any())).thenReturn(true);

        tourDataSyncService.syncAll();

        verify(dataLabClient, never()).fetchDailyVisitors(any());
    }
}
