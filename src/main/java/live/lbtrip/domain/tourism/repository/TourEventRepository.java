package live.lbtrip.domain.tourism.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.tourism.model.entity.TourEvent;

public interface TourEventRepository extends JpaRepository<TourEvent, Long> {

    Optional<TourEvent> findByLocaleAndContentId(Locale locale, String contentId);

    List<TourEvent> findAllByLocaleAndRegionCandidateId(Locale locale, Long regionCandidateId);

    @Query("""
        SELECT e FROM TourEvent e
        WHERE e.locale = :locale AND e.regionCandidate.id = :regionCandidateId AND e.eventEnd >= :today
        ORDER BY e.eventStart ASC, e.eventEnd ASC, e.id ASC
        """)
    List<TourEvent> findActiveByRegion(
        @Param("locale") Locale locale,
        @Param("regionCandidateId") Long regionCandidateId,
        @Param("today") LocalDate today,
        Pageable pageable);
}
