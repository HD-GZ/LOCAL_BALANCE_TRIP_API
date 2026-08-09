package live.lbtrip.domain.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.dto.PopularRegionCode;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.UserFixture;

@DataJpaTest
class RecommendedRegionPopularQueryTest {

    private static final String OTHER_LDONG_REGN_CD = "44";
    private static final String OTHER_LDONG_SIGNGU_CD = "150";

    @Autowired
    private RecommendedRegionRepository recommendedRegionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 추천_수가_많은_지역_코드를_COUNT_내림차순으로_반환한다() {
        User user = userRepository.save(UserFixture.user());
        recommendedRegionRepository.save(region(user, RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD, 1));
        recommendedRegionRepository.save(region(user, RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD, 2));
        recommendedRegionRepository.save(region(user, OTHER_LDONG_REGN_CD, OTHER_LDONG_SIGNGU_CD, 3));
        entityManager.flush();
        entityManager.clear();

        List<PopularRegionCode> result = recommendedRegionRepository.findPopularRegionCodes(PageRequest.of(0, 6));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLdongRegnCd()).isEqualTo(RecommendationFixture.LDONG_REGN_CD);
        assertThat(result.get(0).getLdongSignguCd()).isEqualTo(RecommendationFixture.LDONG_SIGNGU_CD);
        assertThat(result.get(1).getLdongRegnCd()).isEqualTo(OTHER_LDONG_REGN_CD);
        assertThat(result.get(1).getLdongSignguCd()).isEqualTo(OTHER_LDONG_SIGNGU_CD);
    }

    private RecommendedRegion region(User user, String ldongRegnCd, String ldongSignguCd, int displayOrder) {
        return RecommendedRegion.create(
            user,
            RecommendationFixture.REGION_NAME,
            ldongRegnCd,
            ldongSignguCd,
            RecommendationFixture.IMAGE_URL,
            RecommendationFixture.REGION_REASON,
            displayOrder);
    }
}
