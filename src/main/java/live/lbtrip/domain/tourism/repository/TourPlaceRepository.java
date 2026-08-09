package live.lbtrip.domain.tourism.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public interface TourPlaceRepository extends JpaRepository<TourPlace, Long> {

    Optional<TourPlace> findByContentId(String contentId);

    List<TourPlace> findAllByLdongRegnCdAndLdongSignguCdOrderByContentTypeIdAscSortOrderAsc(
        String ldongRegnCd, String ldongSignguCd);

    List<TourPlace> findAllByOverviewIsNull();

    @Query(
        value = "SELECT * FROM tour_places WHERE image_url IS NOT NULL ORDER BY RAND() LIMIT :limit",
        nativeQuery = true)
    List<TourPlace> findRandomWithImage(@Param("limit") int limit);
}
