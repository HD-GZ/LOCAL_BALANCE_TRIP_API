package live.lbtrip.domain.savedcourse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;

public interface TourReceiptRepository extends JpaRepository<TourReceipt, Long> {

    List<TourReceipt> findAllBySavedCourseIdOrderByIdDesc(Long savedCourseId);

    Optional<TourReceipt> findByIdAndSavedCourseId(Long id, Long savedCourseId);
}
