package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
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
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@ExtendWith(MockitoExtension.class)
class TourPlaceSyncerTest {

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private TourPlaceRepository tourPlaceRepository;

    @InjectMocks
    private TourPlaceSyncer tourPlaceSyncer;

    @Nested
    class 장소_적재 {

        @Test
        void 코스_후보_유형마다_로케일로_조회하고_새_장소는_로케일과_함께_저장한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            TourPlaceItem item = new TourPlaceItem("3093358", "Gwangyang Wine Cave (광양와인동굴)",
                TourContentType.TOURIST_SPOT.getCode(), null, 127.6, 34.9);
            when(tourApiClient.fetchPlaces(eq(Locale.ENGLISH), anyString(), anyString(), any()))
                .thenReturn(List.of());
            when(tourApiClient.fetchPlaces(
                Locale.ENGLISH, candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), TourContentType.TOURIST_SPOT))
                .thenReturn(List.of(item));
            when(tourPlaceRepository.findByLocaleAndContentId(Locale.ENGLISH, "3093358")).thenReturn(Optional.empty());

            tourPlaceSyncer.sync(candidate, Locale.ENGLISH);

            for (TourContentType type : TourContentType.courseCandidates()) {
                verify(tourApiClient).fetchPlaces(
                    Locale.ENGLISH, candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), type);
            }
            verify(tourApiClient, never()).fetchPlaces(
                any(), anyString(), anyString(), eq(TourContentType.ACCOMMODATION));
            ArgumentCaptor<TourPlace> captor = ArgumentCaptor.forClass(TourPlace.class);
            verify(tourPlaceRepository).save(captor.capture());
            assertThat(captor.getValue().getLocale()).isEqualTo(Locale.ENGLISH);
            assertThat(captor.getValue().getContentId()).isEqualTo("3093358");
            assertThat(captor.getValue().getContentTypeId()).isEqualTo(TourContentType.TOURIST_SPOT.getCode());
        }

        @Test
        void 같은_로케일에_이미_있는_장소는_갱신한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            TourPlace existing = TourPlace.create(Locale.KOREAN, "100", candidate,
                TourContentType.RESTAURANT.getCode(), "옛이름", null, 127.0, 35.0, 9);
            TourPlaceItem item = new TourPlaceItem("100", "새이름",
                TourContentType.RESTAURANT.getCode(), "https://image/1", 127.1, 35.1);
            when(tourApiClient.fetchPlaces(eq(Locale.KOREAN), anyString(), anyString(), any()))
                .thenReturn(List.of());
            when(tourApiClient.fetchPlaces(
                Locale.KOREAN, candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), TourContentType.RESTAURANT))
                .thenReturn(List.of(item));
            when(tourPlaceRepository.findByLocaleAndContentId(Locale.KOREAN, "100")).thenReturn(Optional.of(existing));

            tourPlaceSyncer.sync(candidate, Locale.KOREAN);

            assertThat(existing.getTitle()).isEqualTo("새이름");
            assertThat(existing.getSortOrder()).isZero();
            verify(tourPlaceRepository).save(existing);
        }
    }

    @Nested
    class 소개_보강 {

        @Test
        void 로케일별로_소개가_없는_장소만_같은_로케일로_조회해_채운다() {
            TourPlace place = TourPlace.create(Locale.ENGLISH, "3093358", RegionCandidateFixture.candidateWithId(),
                12, "Gwangyang Wine Cave", null, 127.6, 34.9, 0);
            when(tourPlaceRepository.findAllByLocaleAndOverviewIsNull(Locale.ENGLISH)).thenReturn(List.of(place));
            when(tourApiClient.fetchOverview(Locale.ENGLISH, "3093358")).thenReturn("Opened in July 2017");

            tourPlaceSyncer.syncOverviews(Locale.ENGLISH);

            assertThat(place.getOverview()).isEqualTo("Opened in July 2017");
            verify(tourPlaceRepository).save(place);
        }
    }
}
