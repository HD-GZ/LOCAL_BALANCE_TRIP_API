package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.PropensityFixture;

@ExtendWith(MockitoExtension.class)
class RecommendationGenerationServiceTest {

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @Mock
    private TourRegionStatsRepository tourRegionStatsRepository;

    @Mock
    private TourPlaceRepository tourPlaceRepository;

    @Mock
    private OdiiThemeRepository odiiThemeRepository;

    @Mock
    private RegionScorer regionScorer;

    @Mock
    private CourseComposer courseComposer;

    @Mock
    private RecommendationStore recommendationStore;

    @Mock
    private PropensityFinder propensityFinder;

    @InjectMocks
    private RecommendationGenerationService recommendationGenerationService;

    @Test
    void 관광_통계가_없으면_데이터_준비_예외를_던진다() {
        when(propensityFinder.findByUserId(AuthResponseFixture.USER_ID))
            .thenReturn(PropensityFixture.propensity());
        when(regionCandidateRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> recommendationGenerationService.createRecommendations(AuthResponseFixture.USER_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TOUR_DATA_NOT_READY);
    }
}
