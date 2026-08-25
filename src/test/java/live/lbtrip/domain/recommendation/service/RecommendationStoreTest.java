package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.PlannedCourse;
import live.lbtrip.domain.recommendation.model.vo.RoutedPlace;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.RegionMetricsFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class RecommendationStoreTest {

    private static final Long USER_ID = AuthResponseFixture.USER_ID;

    @Mock
    private RecommendedRegionRepository recommendedRegionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @InjectMocks
    private RecommendationStore recommendationStore;

    @Test
    void 기존_추천을_삭제하고_새_계획을_스냅샷으로_저장한다() {
        User user = UserFixture.user();
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        RegionPlan plan = RegionPlan.of(
            RegionMetricsFixture.로컬실속_지역(), RegionMetricsFixture.로컬실속_지역().regionName(), "지역 추천 이유",
            List.of(PlannedCourse.of("담양 산책 코스", "코스 이유", List.of(
                RoutedPlace.of(places.get(0), null, RecommendationFixture.PLACE_REASON),
                RoutedPlace.of(places.get(1), 6, null),
                RoutedPlace.of(places.get(2), 10, "시장 이유")))));
        RecommendedRegion existing = RecommendationFixture.region();
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(USER_ID, Locale.KOREAN))
            .thenReturn(List.of(existing));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(regionCandidateRepository.getReferenceById(
            RegionMetricsFixture.로컬실속_지역().regionCandidateId())).thenReturn(regionCandidate);

        recommendationStore.replace(USER_ID, Locale.KOREAN, List.of(plan));

        InOrder order = inOrder(recommendedRegionRepository);
        order.verify(recommendedRegionRepository).deleteAll(List.of(existing));
        order.verify(recommendedRegionRepository).flush();

        ArgumentCaptor<RecommendedRegion> regionCaptor = ArgumentCaptor.forClass(RecommendedRegion.class);
        verify(recommendedRegionRepository).save(regionCaptor.capture());
        RecommendedRegion saved = regionCaptor.getValue();
        assertThat(saved.getRegionName()).isEqualTo("로컬실속");
        assertThat(saved.getReason()).isEqualTo("지역 추천 이유");
        assertThat(saved.getDisplayOrder()).isEqualTo(1);
        assertThat(saved.getImageUrl()).isEqualTo(places.getFirst().getImageUrl());
        assertThat(saved.getRegionCandidate()).isEqualTo(regionCandidate);

        assertThat(saved.getCourses()).singleElement().satisfies(course -> {
            assertThat(course.getName()).isEqualTo("담양 산책 코스");
            assertThat(course.getReason()).isEqualTo("코스 이유");
            assertThat(course.getDisplayOrder()).isEqualTo(1);
            assertThat(course.getImageUrl()).isEqualTo(places.getFirst().getImageUrl());
            assertThat(course.getPlaces()).extracting(CoursePlace::getVisitOrder).containsExactly(1, 2, 3);
            assertThat(course.getPlaces()).extracting(CoursePlace::getWalkMinutes).containsExactly(null, 6, 10);
            assertThat(course.getPlaces()).extracting(CoursePlace::getName)
                .containsExactly("죽녹원", "관방제림", "담양시장");
            assertThat(course.getPlaces()).extracting(CoursePlace::isHasAudio)
                .containsOnly(false);
            assertThat(course.getPlaces()).extracting(CoursePlace::getReason)
                .containsExactly(RecommendationFixture.PLACE_REASON, null, "시장 이유");
        });
    }

    @Test
    void 같은_로케일의_기존_추천만_삭제하고_로케일을_함께_저장한다() {
        User user = UserFixture.user();
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        RegionPlan plan = RegionPlan.of(
            RegionMetricsFixture.로컬실속_지역(), "Damyang-gun, Jeollanam-do", "region reason",
            List.of(PlannedCourse.of("Damyang Walk", "course reason", List.of(
                RoutedPlace.of(places.get(0), null),
                RoutedPlace.of(places.get(1), 6),
                RoutedPlace.of(places.get(2), 10)))));
        RecommendedRegion existingEnglish = RecommendationFixture.region();
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(USER_ID, Locale.ENGLISH))
            .thenReturn(List.of(existingEnglish));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(regionCandidateRepository.getReferenceById(
            RegionMetricsFixture.로컬실속_지역().regionCandidateId())).thenReturn(regionCandidate);

        recommendationStore.replace(USER_ID, Locale.ENGLISH, List.of(plan));

        verify(recommendedRegionRepository).deleteAll(List.of(existingEnglish));
        verify(recommendedRegionRepository, never()).findAllByUserIdAndLocaleOrderByDisplayOrder(USER_ID, Locale.KOREAN);
        ArgumentCaptor<RecommendedRegion> regionCaptor = ArgumentCaptor.forClass(RecommendedRegion.class);
        verify(recommendedRegionRepository).save(regionCaptor.capture());
        assertThat(regionCaptor.getValue().getLocale()).isEqualTo(Locale.ENGLISH);
        assertThat(regionCaptor.getValue().getRegionName()).isEqualTo("Damyang-gun, Jeollanam-do");
    }

    @Test
    void 매칭된_오디오_테마의_URL을_스냅샷에_복사한다() {
        User user = UserFixture.user();
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        OdiiTheme theme = OdiiTheme.create("t1", "l1", "죽녹원", 126.986, 35.325);
        theme.updateAudio("https://audio.example.com/guide.mp3", LocalDateTime.now());
        places.getFirst().assignOdiiTheme(theme);
        RegionPlan plan = RegionPlan.of(
            RegionMetricsFixture.로컬실속_지역(), RegionMetricsFixture.로컬실속_지역().regionName(), "지역 추천 이유",
            List.of(PlannedCourse.of("담양 산책 코스", "코스 이유", List.of(
                RoutedPlace.of(places.get(0), null),
                RoutedPlace.of(places.get(1), 6),
                RoutedPlace.of(places.get(2), 10)))));
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(USER_ID, Locale.KOREAN)).thenReturn(List.of());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(regionCandidateRepository.getReferenceById(
            RegionMetricsFixture.로컬실속_지역().regionCandidateId())).thenReturn(regionCandidate);

        recommendationStore.replace(USER_ID, Locale.KOREAN, List.of(plan));

        ArgumentCaptor<RecommendedRegion> regionCaptor = ArgumentCaptor.forClass(RecommendedRegion.class);
        verify(recommendedRegionRepository).save(regionCaptor.capture());
        List<CoursePlace> savedPlaces = regionCaptor.getValue().getCourses().getFirst().getPlaces();
        assertThat(savedPlaces).extracting(CoursePlace::isHasAudio).containsExactly(true, false, false);
        assertThat(savedPlaces.getFirst().getAudioUrl()).isEqualTo("https://audio.example.com/guide.mp3");
    }

    @Test
    void 지역과_코스의_노출_순서를_계획_순서대로_부여한다() {
        User user = UserFixture.user();
        RegionCandidate regionCandidate = RegionCandidateFixture.candidateWithId();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        List<RoutedPlace> routed = List.of(
            RoutedPlace.of(places.get(0), null),
            RoutedPlace.of(places.get(1), 6),
            RoutedPlace.of(places.get(2), 10));
        RegionPlan first = RegionPlan.of(
            RegionMetricsFixture.로컬실속_지역(), RegionMetricsFixture.로컬실속_지역().regionName(), "이유",
            List.of(PlannedCourse.of("코스A", "이유", routed), PlannedCourse.of("코스B", "이유", routed)));
        RegionPlan second = RegionPlan.of(
            RegionMetricsFixture.체험활동_지역(), RegionMetricsFixture.체험활동_지역().regionName(), "이유",
            List.of(PlannedCourse.of("코스C", "이유", routed)));
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(USER_ID, Locale.KOREAN)).thenReturn(List.of());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(regionCandidateRepository.getReferenceById(
            RegionMetricsFixture.로컬실속_지역().regionCandidateId())).thenReturn(regionCandidate);
        when(regionCandidateRepository.getReferenceById(
            RegionMetricsFixture.체험활동_지역().regionCandidateId())).thenReturn(regionCandidate);

        recommendationStore.replace(USER_ID, Locale.KOREAN, List.of(first, second));

        ArgumentCaptor<RecommendedRegion> regionCaptor = ArgumentCaptor.forClass(RecommendedRegion.class);
        verify(recommendedRegionRepository, times(2)).save(regionCaptor.capture());
        List<RecommendedRegion> savedRegions = regionCaptor.getAllValues();
        assertThat(savedRegions).extracting(RecommendedRegion::getDisplayOrder).containsExactly(1, 2);
        assertThat(savedRegions.getFirst().getCourses())
            .extracting(GeneratedCourse::getDisplayOrder).containsExactly(1, 2);
    }
}
