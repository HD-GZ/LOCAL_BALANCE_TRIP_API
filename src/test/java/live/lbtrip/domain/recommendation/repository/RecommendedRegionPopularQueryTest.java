package live.lbtrip.domain.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.dto.PopularRegion;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.UserFixture;

@DataJpaTest
class RecommendedRegionPopularQueryTest {

    @Autowired
    private RecommendedRegionRepository recommendedRegionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 추천_수가_많은_지역_후보를_COUNT_내림차순으로_반환한다() {
        User user = userRepository.save(UserFixture.user());
        RegionCandidate popular = persistCandidate(RegionCandidateFixture.candidate());
        RegionCandidate other = persistCandidate(
            RegionCandidate.create("충청남도 홍성군", "44", "150"));
        recommendedRegionRepository.save(region(user, popular, 1));
        recommendedRegionRepository.save(region(user, popular, 2));
        recommendedRegionRepository.save(region(user, other, 3));
        entityManager.flush();
        entityManager.clear();

        List<PopularRegion> result = recommendedRegionRepository.findPopularRegions(Locale.KOREAN, PageRequest.of(0, 6));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRegionCandidateId()).isEqualTo(popular.getId());
        assertThat(result.get(1).getRegionCandidateId()).isEqualTo(other.getId());
    }

    @Test
    void 다른_로케일의_추천은_집계에서_제외한다() {
        User user = userRepository.save(UserFixture.user());
        RegionCandidate candidate = persistCandidate(RegionCandidateFixture.candidate());
        recommendedRegionRepository.save(region(user, candidate, 1));
        recommendedRegionRepository.save(RecommendedRegion.create(
            user, Locale.ENGLISH, "Damyang-gun, Jeollanam-do", candidate,
            RecommendationFixture.IMAGE_URL, RecommendationFixture.REGION_REASON, 2));
        entityManager.flush();
        entityManager.clear();

        assertThat(recommendedRegionRepository.findPopularRegions(Locale.ENGLISH, PageRequest.of(0, 6))).hasSize(1);
        assertThat(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(user.getId(), Locale.ENGLISH))
            .singleElement()
            .extracting(RecommendedRegion::getRegionName)
            .isEqualTo("Damyang-gun, Jeollanam-do");
    }

    private RegionCandidate persistCandidate(RegionCandidate candidate) {
        entityManager.persist(candidate);
        return candidate;
    }

    private RecommendedRegion region(User user, RegionCandidate candidate, int displayOrder) {
        return RecommendedRegion.create(
            user,
            Locale.KOREAN,
            RecommendationFixture.REGION_NAME,
            candidate,
            RecommendationFixture.IMAGE_URL,
            RecommendationFixture.REGION_REASON,
            displayOrder);
    }
}
