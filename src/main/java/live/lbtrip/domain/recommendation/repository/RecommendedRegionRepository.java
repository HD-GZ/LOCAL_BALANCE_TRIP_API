package live.lbtrip.domain.recommendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;

public interface RecommendedRegionRepository extends JpaRepository<RecommendedRegion, Long> {

    List<RecommendedRegion> findAllByUserIdOrderByDisplayOrder(Long userId);
}
