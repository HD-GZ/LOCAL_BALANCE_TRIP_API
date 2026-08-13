package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.client.dto.TourPlacePage;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@ExtendWith(MockitoExtension.class)
class TourPlaceSyncerTest {

    private static final int PLACES_PER_CONTENT_TYPE = 15;

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private TourPlaceRepository tourPlaceRepository;

    @InjectMocks
    private TourPlaceSyncer tourPlaceSyncer;

    private final RegionCandidate candidate = RegionCandidateFixture.candidateWithId();

    @Nested
    class 장소_선정 {

        @Test
        void 콘텐츠타입별로_최대_15개까지만_저장한다() {
            givenPage(itemsOf(TourContentType.TOURIST_SPOT, 20));
            givenNoExistingPlace();

            List<TourPlaceItem> synced = tourPlaceSyncer.sync(candidate);

            assertThat(synced).hasSize(PLACES_PER_CONTENT_TYPE);
            verify(tourPlaceRepository, times(PLACES_PER_CONTENT_TYPE)).save(any(TourPlace.class));
        }

        @Test
        void 응답_순서대로_타입_내에서_sortOrder를_매긴다() {
            givenPage(itemsOf(TourContentType.TOURIST_SPOT, 3));
            givenNoExistingPlace();

            tourPlaceSyncer.sync(candidate);

            assertThat(savedPlaces(3))
                .extracting(TourPlace::getSortOrder)
                .containsExactly(0, 1, 2);
        }

        @Test
        void 타입이_섞인_페이지를_타입별로_나눠_각각_0부터_번호를_매긴다() {
            givenPage(List.of(
                item(TourContentType.TOURIST_SPOT, 0),
                item(TourContentType.RESTAURANT, 0),
                item(TourContentType.TOURIST_SPOT, 1)));
            givenNoExistingPlace();

            tourPlaceSyncer.sync(candidate);

            assertThat(savedPlaces(3))
                .extracting(TourPlace::getContentTypeId, TourPlace::getSortOrder)
                .containsExactlyInAnyOrder(
                    tuple(TourContentType.TOURIST_SPOT.getCode(), 0),
                    tuple(TourContentType.TOURIST_SPOT.getCode(), 1),
                    tuple(TourContentType.RESTAURANT.getCode(), 0));
        }

        @Test
        void 코스_후보가_아닌_숙박은_저장하지_않는다() {
            givenPage(itemsOf(TourContentType.ACCOMMODATION, 5));

            List<TourPlaceItem> synced = tourPlaceSyncer.sync(candidate);

            assertThat(synced).isEmpty();
            verify(tourPlaceRepository, never()).save(any(TourPlace.class));
        }
    }

    private void givenPage(List<TourPlaceItem> items) {
        when(tourApiClient.fetchPlaces(candidate))
            .thenReturn(TourPlacePage.of(items.size(), items));
    }

    private void givenNoExistingPlace() {
        when(tourPlaceRepository.findByContentId(any())).thenReturn(Optional.empty());
    }

    private List<TourPlace> savedPlaces(int expectedCount) {
        ArgumentCaptor<TourPlace> captor = ArgumentCaptor.forClass(TourPlace.class);
        verify(tourPlaceRepository, times(expectedCount)).save(captor.capture());
        return captor.getAllValues();
    }

    private List<TourPlaceItem> itemsOf(TourContentType contentType, int count) {
        List<TourPlaceItem> items = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            items.add(item(contentType, index));
        }
        return items;
    }

    private TourPlaceItem item(TourContentType contentType, int index) {
        return new TourPlaceItem(
            contentType.getCode() + "-" + index,
            contentType.getKoreanName() + index,
            contentType.getCode(),
            "https://image/" + index,
            126.9 + index,
            35.3 + index
        );
    }
}
