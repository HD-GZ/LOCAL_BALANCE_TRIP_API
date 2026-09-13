package live.lbtrip.domain.tourism.repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public interface TourPlaceRepository extends JpaRepository<TourPlace, Long> {

    Optional<TourPlace> findByLocaleAndContentId(Locale locale, String contentId);

    List<TourPlace> findAllByLocaleAndRegionCandidateIdOrderByContentTypeIdAscSortOrderAsc(
        Locale locale, Long regionCandidateId);

    @Query("""
        SELECT p FROM TourPlace p LEFT JOIN FETCH p.odiiTheme
        WHERE p.locale = :locale AND p.regionCandidate.id = :regionCandidateId
        ORDER BY p.contentTypeId ASC, p.sortOrder ASC
        """)
    List<TourPlace> findAllWithOdiiThemeByLocaleAndRegionCandidateId(
        @Param("locale") Locale locale, @Param("regionCandidateId") Long regionCandidateId);

    List<TourPlace> findAllByLocaleAndOverviewIsNull(Locale locale);

    @Query("""
        SELECT p FROM TourPlace p
        WHERE p.locale = :locale AND p.odiiTheme IS NULL AND p.ttsSyncedAt IS NULL
          AND p.overview IS NOT NULL AND TRIM(p.overview) <> ''
        ORDER BY p.id ASC
        """)
    Page<TourPlace> findTtsPending(@Param("locale") Locale locale, Pageable pageable);

    @Query(
        value = "SELECT * FROM tour_places WHERE locale = :locale AND image_url IS NOT NULL ORDER BY RAND() LIMIT :limit",
        nativeQuery = true)
    List<TourPlace> findRandomWithImage(@Param("locale") String locale, @Param("limit") int limit);
}
