package live.lbtrip.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.TourContentType;

public interface TourContentTypeRepository extends JpaRepository<TourContentType, Long> {
}
