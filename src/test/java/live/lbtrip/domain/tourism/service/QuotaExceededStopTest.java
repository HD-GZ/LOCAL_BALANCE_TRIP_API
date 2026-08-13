package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.DataLabClient;
import live.lbtrip.domain.tourism.client.OdiiClient;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TourPlaceFixture;

@ExtendWith(MockitoExtension.class)
class QuotaExceededStopTest {

    private static final BusinessException QUOTA_EXCEEDED =
        BusinessException.of(ErrorCode.TOUR_API_QUOTA_EXCEEDED);

    @Nested
    @ExtendWith(MockitoExtension.class)
    class overview_보강 {

        @Mock
        private TourApiClient tourApiClient;

        @Mock
        private TourPlaceRepository tourPlaceRepository;

        @InjectMocks
        private TourPlaceSyncer tourPlaceSyncer;

        @Test
        void 한도_초과를_만나면_남은_장소는_호출하지_않는다() {
            List<TourPlace> pending = List.of(
                TourPlaceFixture.withImage("첫번째", "https://image/1"),
                TourPlaceFixture.withImage("두번째", "https://image/2"),
                TourPlaceFixture.withImage("세번째", "https://image/3"));
            when(tourPlaceRepository.findAllByOverviewIsNull()).thenReturn(pending);
            when(tourApiClient.fetchOverview(any()))
                .thenReturn("첫 장소 설명")
                .thenThrow(QUOTA_EXCEEDED);

            tourPlaceSyncer.syncOverviews();

            verify(tourApiClient, times(2)).fetchOverview(any());
            verify(tourPlaceRepository, times(1)).save(any(TourPlace.class));
        }

        @Test
        void 개별_장소_실패는_다음_장소로_계속_진행한다() {
            List<TourPlace> pending = List.of(
                TourPlaceFixture.withImage("첫번째", "https://image/1"),
                TourPlaceFixture.withImage("두번째", "https://image/2"));
            when(tourPlaceRepository.findAllByOverviewIsNull()).thenReturn(pending);
            when(tourApiClient.fetchOverview(any()))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE))
                .thenReturn("두번째 장소 설명");

            tourPlaceSyncer.syncOverviews();

            verify(tourApiClient, times(2)).fetchOverview(any());
            verify(tourPlaceRepository, times(1)).save(any(TourPlace.class));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class 오디오_보강 {

        @Mock
        private OdiiClient odiiClient;

        @Mock
        private OdiiThemeRepository odiiThemeRepository;

        @InjectMocks
        private OdiiThemeSyncer odiiThemeSyncer;

        @Test
        void 한도_초과를_만나면_남은_테마는_audioSyncedAt을_찍지_않는다() {
            List<OdiiTheme> pending = List.of(
                OdiiTheme.create("tid1", "tlid1", "테마1", 126.9, 35.3),
                OdiiTheme.create("tid2", "tlid2", "테마2", 126.9, 35.3),
                OdiiTheme.create("tid3", "tlid3", "테마3", 126.9, 35.3));
            when(odiiThemeRepository.findAllByAudioSyncedAtIsNull()).thenReturn(pending);
            when(odiiClient.fetchFirstAudioUrl(any(), any()))
                .thenReturn("https://audio/1")
                .thenThrow(QUOTA_EXCEEDED);

            odiiThemeSyncer.syncAudioUrls();

            verify(odiiThemeRepository, times(1)).save(any(OdiiTheme.class));
            assertThat(pending.get(1).getAudioSyncedAt()).isNull();
            assertThat(pending.get(2).getAudioSyncedAt()).isNull();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class 방문자수_적재 {

        @Mock
        private RegionCandidateRepository regionCandidateRepository;

        @Mock
        private DataLabClient dataLabClient;

        @Mock
        private RegionVisitorStatsRepository regionVisitorStatsRepository;

        @InjectMocks
        private VisitorStatsSyncer visitorStatsSyncer;

        @Test
        void 한도_초과를_만나면_남은_일자는_조회하지_않는다() {
            when(regionCandidateRepository.findAll())
                .thenReturn(List.of(RegionCandidateFixture.candidateWithId()));
            when(regionVisitorStatsRepository.existsByBaseDate(any())).thenReturn(false);
            when(dataLabClient.fetchDailyVisitors(any())).thenThrow(QUOTA_EXCEEDED);

            visitorStatsSyncer.sync();

            verify(dataLabClient, times(1)).fetchDailyVisitors(any(LocalDate.class));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class 지역별_적재 {

        @Mock
        private RegionCandidateRepository regionCandidateRepository;

        @Mock
        private RegionStatsSyncer regionStatsSyncer;

        @Mock
        private TourPlaceSyncer tourPlaceSyncer;

        @Mock
        private OdiiThemeSyncer odiiThemeSyncer;

        @Mock
        private PlaceThemeLinker placeThemeLinker;

        @Mock
        private VisitorStatsSyncer visitorStatsSyncer;

        @InjectMocks
        private TourDataSyncService tourDataSyncService;

        @Test
        void 한도_초과를_만나면_남은_지역은_적재하지_않는다() {
            RegionCandidate first = RegionCandidateFixture.candidate();
            RegionCandidate second = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(first, second));
            doThrow(QUOTA_EXCEEDED).when(regionStatsSyncer).sync(first);

            tourDataSyncService.syncAll();

            verify(regionStatsSyncer, never()).sync(second);
        }
    }
}
