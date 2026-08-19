package live.lbtrip.domain.tourism.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.model.enums.TourSyncStep;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@ExtendWith(MockitoExtension.class)
class TourDataSyncServiceTest {

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

    @Nested
    class 전체_적재 {

        @Test
        void 지역별_적재를_순서대로_호출하고_마지막에_전역_보강을_수행한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(candidate));
            when(tourPlaceSyncer.sync(candidate, Locale.KOREAN)).thenReturn(List.of());

            tourDataSyncService.syncAll();

            verify(regionStatsSyncer).sync(candidate);
            verify(tourPlaceSyncer).sync(candidate, Locale.KOREAN);
            verify(odiiThemeSyncer).sync(List.of());
            verify(placeThemeLinker).link(candidate);
            verify(tourPlaceSyncer).syncOverviews(Locale.KOREAN);
            verify(odiiThemeSyncer).syncAudioUrls();
            verify(visitorStatsSyncer).sync();
            verify(tourPlaceSyncer).sync(candidate, Locale.ENGLISH);
            verify(tourPlaceSyncer).syncOverviews(Locale.ENGLISH);
        }

        @Test
        void 모든_지역의_테마_적재가_끝난_뒤에_장소_테마를_매칭한다() {
            RegionCandidate first = RegionCandidateFixture.candidate();
            RegionCandidate second = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(first, second));
            when(tourPlaceSyncer.sync(any(), eq(Locale.KOREAN))).thenReturn(List.of());

            tourDataSyncService.syncAll();

            InOrder inOrder = inOrder(odiiThemeSyncer, placeThemeLinker);
            inOrder.verify(odiiThemeSyncer, times(2)).sync(anyList());
            inOrder.verify(placeThemeLinker).link(first);
            inOrder.verify(placeThemeLinker).link(second);
        }

        @Test
        void 한_지역이_실패해도_다음_지역과_전역_보강을_계속_진행한다() {
            RegionCandidate failing = RegionCandidateFixture.candidate();
            RegionCandidate succeeding = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(failing, succeeding));
            doThrow(BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE))
                .when(regionStatsSyncer).sync(failing);
            when(tourPlaceSyncer.sync(succeeding, Locale.KOREAN)).thenReturn(List.of());

            tourDataSyncService.syncAll();

            verify(regionStatsSyncer).sync(succeeding);
            verify(tourPlaceSyncer, never()).sync(failing, Locale.KOREAN);
            verify(visitorStatsSyncer).sync();
        }

        @Test
        void 매칭이_실패한_지역이_있어도_나머지_지역과_전역_보강을_계속_진행한다() {
            RegionCandidate failing = RegionCandidateFixture.candidate();
            RegionCandidate succeeding = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(failing, succeeding));
            when(tourPlaceSyncer.sync(any(), eq(Locale.KOREAN))).thenReturn(List.of());
            doThrow(BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE))
                .when(placeThemeLinker).link(failing);

            tourDataSyncService.syncAll();

            verify(placeThemeLinker).link(succeeding);
            verify(tourPlaceSyncer).syncOverviews(Locale.KOREAN);
            verify(visitorStatsSyncer).sync();
        }

        @Test
        void OVERVIEWS_단계만_실행하면_다른_단계는_수행하지_않는다() {
            tourDataSyncService.sync(TourSyncStep.OVERVIEWS);

            verify(tourPlaceSyncer).syncOverviews(Locale.KOREAN);
            verify(tourPlaceSyncer, never()).syncOverviews(Locale.ENGLISH);
            verify(odiiThemeSyncer, never()).syncAudioUrls();
            verify(visitorStatsSyncer, never()).sync();
            verify(regionStatsSyncer, never()).sync(any());
        }

        @Test
        void PLACES_EN_단계는_지역별로_영문_장소를_적재하고_한_지역이_실패해도_계속_진행한다() {
            RegionCandidate failing = RegionCandidateFixture.candidate();
            RegionCandidate succeeding = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(failing, succeeding));
            doThrow(BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE))
                .when(tourPlaceSyncer).sync(failing, Locale.ENGLISH);

            tourDataSyncService.sync(TourSyncStep.PLACES_EN);

            verify(tourPlaceSyncer).sync(succeeding, Locale.ENGLISH);
            verify(tourPlaceSyncer, never()).syncOverviews(any());
            verify(tourPlaceSyncer, never()).sync(any(), eq(Locale.KOREAN));
            verify(regionStatsSyncer, never()).sync(any());
        }

        @Test
        void PLACES_EN_단계에서_한도_초과를_만나면_남은_지역은_적재하지_않는다() {
            RegionCandidate first = RegionCandidateFixture.candidate();
            RegionCandidate second = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(first, second));
            doThrow(BusinessException.of(ErrorCode.TOUR_API_QUOTA_EXCEEDED))
                .when(tourPlaceSyncer).sync(first, Locale.ENGLISH);

            tourDataSyncService.sync(TourSyncStep.PLACES_EN);

            verify(tourPlaceSyncer, never()).sync(second, Locale.ENGLISH);
        }

        @Test
        void OVERVIEWS_EN_단계는_영문_소개만_보강한다() {
            tourDataSyncService.sync(TourSyncStep.OVERVIEWS_EN);

            verify(tourPlaceSyncer).syncOverviews(Locale.ENGLISH);
            verify(tourPlaceSyncer, never()).sync(any(), any());
            verify(tourPlaceSyncer, never()).syncOverviews(Locale.KOREAN);
        }

        @Test
        void VISITOR_STATS_단계는_외부_지역_조회_없이_방문자수만_적재한다() {
            tourDataSyncService.sync(TourSyncStep.VISITOR_STATS);

            verify(visitorStatsSyncer).sync();
            verify(regionCandidateRepository, never()).findAll();
            verify(tourPlaceSyncer, never()).syncOverviews(any());
        }

        @Test
        void 지역_후보가_없어도_전역_보강은_수행한다() {
            when(regionCandidateRepository.findAll()).thenReturn(List.of());

            tourDataSyncService.syncAll();

            verify(tourPlaceSyncer).syncOverviews(Locale.KOREAN);
            verify(odiiThemeSyncer).syncAudioUrls();
            verify(visitorStatsSyncer).sync();
            verify(regionStatsSyncer, never()).sync(any());
        }
    }
}
