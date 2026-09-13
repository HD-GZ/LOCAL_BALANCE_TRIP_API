package live.lbtrip.domain.recommendation.repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.dto.PopularRegion;

public interface RecommendedRegionRepository extends JpaRepository<RecommendedRegion, Long> {

    List<RecommendedRegion> findAllByUserId(Long userId);

    List<RecommendedRegion> findAllByUserIdAndLocaleOrderByDisplayOrder(Long userId, Locale locale);

    Optional<RecommendedRegion> findByIdAndUserIdAndLocale(Long id, Long userId, Locale locale);

    Optional<RecommendedRegion> findFirstByRegionCandidateIdAndLocale(Long regionCandidateId, Locale locale);

    @Query(
        "SELECT r.regionCandidate.id AS regionCandidateId "
            + "FROM RecommendedRegion r "
            + "WHERE r.locale = :locale "
            + "GROUP BY r.regionCandidate.id "
            + "ORDER BY COUNT(r) DESC, r.regionCandidate.id ASC")
    List<PopularRegion> findPopularRegions(@Param("locale") Locale locale, Pageable pageable);
}
