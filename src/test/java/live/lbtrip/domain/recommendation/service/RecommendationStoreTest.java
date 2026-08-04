package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class RecommendationStoreTest {

    @Mock
    private RecommendedRegionRepository recommendedRegionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecommendationStore recommendationStore;

    @Test
    void 지역과_코스에_입력_순서대로_표시_순서를_부여한다() {
        when(recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(AuthResponseFixture.USER_ID))
            .thenReturn(List.of());
        when(userRepository.getReferenceById(AuthResponseFixture.USER_ID)).thenReturn(UserFixture.user());
        ArgumentCaptor<RecommendedRegion> regionCaptor = ArgumentCaptor.forClass(RecommendedRegion.class);
        when(recommendedRegionRepository.save(regionCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        recommendationStore.replace(AuthResponseFixture.USER_ID, List.of(
            plan("첫 지역", List.of(course("첫 코스"), course("두 번째 코스"))),
            plan("두 번째 지역", List.of(course("세 번째 코스")))
        ));

        assertThat(regionCaptor.getAllValues()).extracting(RecommendedRegion::getDisplayOrder)
            .containsExactly(1, 2);
        assertThat(regionCaptor.getAllValues().getFirst().getCourses())
            .extracting(course -> course.getDisplayOrder())
            .containsExactly(1, 2);
    }

    private RegionPlan plan(String regionName, List<RegionPlan.CoursePlanData> courses) {
        return RegionPlan.of(
            regionName,
            RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD,
            RecommendationFixture.IMAGE_URL,
            RecommendationFixture.REGION_REASON,
            courses
        );
    }

    private RegionPlan.CoursePlanData course(String name) {
        return RegionPlan.CoursePlanData.of(
            name,
            RecommendationFixture.COURSE_REASON,
            RecommendationFixture.IMAGE_URL,
            List.of(RegionPlan.PlaceSnapshot.of(
                1, "장소", null, RecommendationFixture.IMAGE_URL,
                35.0, 127.0, null, false, null))
        );
    }
}
