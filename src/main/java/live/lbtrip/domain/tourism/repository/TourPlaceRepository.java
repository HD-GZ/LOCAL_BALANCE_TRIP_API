package live.lbtrip.domain.tourism.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public interface TourPlaceRepository extends JpaRepository<TourPlace, Long> {

    Optional<TourPlace> findByContentId(String contentId);

    List<TourPlace> findAllByLdongRegnCdAndLdongSignguCdOrderByContentTypeIdAscSortOrderAsc(
        String ldongRegnCd, String ldongSignguCd);

    List<TourPlace> findAllByOverviewIsNull();
}
