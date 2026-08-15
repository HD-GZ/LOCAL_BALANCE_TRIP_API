package live.lbtrip.domain.savedcourse.receipt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;

public interface TourReceiptRepository extends JpaRepository<TourReceipt, Long> {

    List<TourReceipt> findAllBySavedCourseUserId(Long userId);
}
