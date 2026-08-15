package live.lbtrip.domain.tourism.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public interface TourPlaceRepository extends JpaRepository<TourPlace, Long> {

    Optional<TourPlace> findByContentId(String contentId);

    List<TourPlace> findAllByRegionCandidateIdOrderByContentTypeIdAscSortOrderAsc(Long regionCandidateId);

    @Query("""
        SELECT p FROM TourPlace p LEFT JOIN FETCH p.odiiTheme
        WHERE p.regionCandidate.id = :regionCandidateId
        ORDER BY p.contentTypeId ASC, p.sortOrder ASC
        """)
    List<TourPlace> findAllWithOdiiThemeByRegionCandidateId(@Param("regionCandidateId") Long regionCandidateId);

    List<TourPlace> findAllByOverviewIsNull();

    @Query(
        value = "SELECT * FROM tour_places WHERE image_url IS NOT NULL ORDER BY RAND() LIMIT :limit",
        nativeQuery = true)
    List<TourPlace> findRandomWithImage(@Param("limit") int limit);
}
