package live.lbtrip.domain.tourism.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
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
            when(tourPlaceSyncer.sync(candidate)).thenReturn(List.of());

            tourDataSyncService.syncAll();

            verify(regionStatsSyncer).sync(candidate);
            verify(tourPlaceSyncer).sync(candidate);
            verify(odiiThemeSyncer).sync(List.of());
            verify(placeThemeLinker).link(candidate);
            verify(tourPlaceSyncer).syncOverviews();
            verify(odiiThemeSyncer).syncAudioUrls();
            verify(visitorStatsSyncer).sync();
        }

        @Test
        void 한_지역이_실패해도_다음_지역과_전역_보강을_계속_진행한다() {
            RegionCandidate failing = RegionCandidateFixture.candidate();
            RegionCandidate succeeding = RegionCandidateFixture.candidateWithId();
            when(regionCandidateRepository.findAll()).thenReturn(List.of(failing, succeeding));
            doThrow(BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE))
                .when(regionStatsSyncer).sync(failing);
            when(tourPlaceSyncer.sync(succeeding)).thenReturn(List.of());

            tourDataSyncService.syncAll();

            verify(regionStatsSyncer).sync(succeeding);
            verify(placeThemeLinker).link(succeeding);
            verify(placeThemeLinker, never()).link(failing);
            verify(visitorStatsSyncer).sync();
        }

        @Test
        void 지역_후보가_없어도_전역_보강은_수행한다() {
            when(regionCandidateRepository.findAll()).thenReturn(List.of());

            tourDataSyncService.syncAll();

            verify(tourPlaceSyncer).syncOverviews();
            verify(odiiThemeSyncer).syncAudioUrls();
            verify(visitorStatsSyncer).sync();
            verify(regionStatsSyncer, never()).sync(any());
        }
    }
}
