package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.EnglishPlaceItem;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TourPlaceFixture;

@ExtendWith(MockitoExtension.class)
class EnglishPlaceSyncerTest {

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private TourPlaceRepository tourPlaceRepository;

    @Mock
    private EnglishPlaceMatcher englishPlaceMatcher;

    @InjectMocks
    private EnglishPlaceSyncer englishPlaceSyncer;

    @Nested
    class 영문_장소_적재 {

        @Test
        void 코스_후보_유형마다_영문_코드로_조회하고_매칭된_장소에_영문_정보를_저장한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            TourPlace place = TourPlaceFixture.withImage("광양와인동굴", "https://image/1");
            EnglishPlaceItem item = new EnglishPlaceItem("3093358", "Gwangyang Wine Cave (광양와인동굴)", 127.6, 34.9);
            when(tourPlaceRepository.findAllByRegionCandidateIdOrderByContentTypeIdAscSortOrderAsc(candidate.getId()))
                .thenReturn(List.of(place));
            when(tourApiClient.fetchEnglishPlaces(anyString(), anyString(), anyInt())).thenReturn(List.of());
            when(tourApiClient.fetchEnglishPlaces(
                candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), TourContentType.TOURIST_SPOT.getEngCode()))
                .thenReturn(List.of(item));
            when(englishPlaceMatcher.match(eq(item), anyList())).thenReturn(Optional.of(place));

            englishPlaceSyncer.sync(candidate);

            for (TourContentType type : TourContentType.courseCandidates()) {
                verify(tourApiClient).fetchEnglishPlaces(
                    candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), type.getEngCode());
            }
            verify(tourApiClient, never()).fetchEnglishPlaces(
                anyString(), anyString(), eq(TourContentType.ACCOMMODATION.getEngCode()));
            assertThat(place.getEngContentId()).isEqualTo("3093358");
            assertThat(place.getTitleEn()).isEqualTo("Gwangyang Wine Cave (광양와인동굴)");
            verify(tourPlaceRepository).save(place);
        }

        @Test
        void 매칭_후보는_같은_유형의_장소로_한정한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            TourPlace spot = TourPlace.create("1", candidate, TourContentType.TOURIST_SPOT.getCode(),
                "관광지", null, 127.6, 34.9, 0);
            TourPlace restaurant = TourPlace.create("2", candidate, TourContentType.RESTAURANT.getCode(),
                "식당", null, 127.6, 34.9, 0);
            EnglishPlaceItem item = new EnglishPlaceItem("900", "Jinseon (진선)", 127.6, 34.9);
            when(tourPlaceRepository.findAllByRegionCandidateIdOrderByContentTypeIdAscSortOrderAsc(candidate.getId()))
                .thenReturn(List.of(spot, restaurant));
            when(tourApiClient.fetchEnglishPlaces(anyString(), anyString(), anyInt())).thenReturn(List.of());
            when(tourApiClient.fetchEnglishPlaces(
                candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), TourContentType.RESTAURANT.getEngCode()))
                .thenReturn(List.of(item));
            when(englishPlaceMatcher.match(item, List.of(restaurant))).thenReturn(Optional.empty());

            englishPlaceSyncer.sync(candidate);

            verify(englishPlaceMatcher).match(item, List.of(restaurant));
            verify(tourPlaceRepository, never()).save(any());
        }
    }

    @Nested
    class 영문_소개_보강 {

        @Test
        void 영문_소개가_없는_장소만_영문_콘텐츠_ID로_조회해_채운다() {
            TourPlace place = TourPlaceFixture.withImage("광양와인동굴", "https://image/1");
            place.updateEnglish("3093358", "Gwangyang Wine Cave (광양와인동굴)");
            when(tourPlaceRepository.findAllByEngContentIdIsNotNullAndOverviewEnIsNull()).thenReturn(List.of(place));
            when(tourApiClient.fetchEnglishOverview("3093358")).thenReturn("Opened in July 2017");

            englishPlaceSyncer.syncOverviews();

            assertThat(place.getOverviewEn()).isEqualTo("Opened in July 2017");
            verify(tourPlaceRepository).save(place);
        }

        @Test
        void 소개가_없으면_빈_문자열로_저장해_재조회를_막는다() {
            TourPlace place = TourPlaceFixture.withImage("광양와인동굴", "https://image/1");
            place.updateEnglish("3093358", "Gwangyang Wine Cave (광양와인동굴)");
            when(tourPlaceRepository.findAllByEngContentIdIsNotNullAndOverviewEnIsNull()).thenReturn(List.of(place));
            when(tourApiClient.fetchEnglishOverview("3093358")).thenReturn(null);

            englishPlaceSyncer.syncOverviews();

            assertThat(place.getOverviewEn()).isEmpty();
        }

        @Test
        void 한도_초과를_만나면_남은_장소는_호출하지_않는다() {
            List<TourPlace> pending = List.of(
                TourPlaceFixture.withImage("첫번째", "https://image/1"),
                TourPlaceFixture.withImage("두번째", "https://image/2"),
                TourPlaceFixture.withImage("세번째", "https://image/3"));
            pending.forEach(place -> place.updateEnglish("e-" + place.getTitle(), place.getTitle()));
            when(tourPlaceRepository.findAllByEngContentIdIsNotNullAndOverviewEnIsNull()).thenReturn(pending);
            when(tourApiClient.fetchEnglishOverview(any()))
                .thenReturn("first")
                .thenThrow(BusinessException.of(ErrorCode.TOUR_API_QUOTA_EXCEEDED));

            englishPlaceSyncer.syncOverviews();

            verify(tourApiClient, times(2)).fetchEnglishOverview(any());
            verify(tourPlaceRepository, times(1)).save(any(TourPlace.class));
        }

        @Test
        void 개별_장소_실패는_다음_장소로_계속_진행한다() {
            List<TourPlace> pending = List.of(
                TourPlaceFixture.withImage("첫번째", "https://image/1"),
                TourPlaceFixture.withImage("두번째", "https://image/2"));
            pending.forEach(place -> place.updateEnglish("e-" + place.getTitle(), place.getTitle()));
            when(tourPlaceRepository.findAllByEngContentIdIsNotNullAndOverviewEnIsNull()).thenReturn(pending);
            when(tourApiClient.fetchEnglishOverview(any()))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE))
                .thenReturn("second");

            englishPlaceSyncer.syncOverviews();

            verify(tourApiClient, times(2)).fetchEnglishOverview(any());
            verify(tourPlaceRepository, times(1)).save(any(TourPlace.class));
        }
    }
}
