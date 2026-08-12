package live.lbtrip.domain.tourism.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.tourism.model.entity.CategoryGroupCode;

public interface CategoryGroupCodeRepository extends JpaRepository<CategoryGroupCode, Long> {
}
