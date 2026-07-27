package live.lbtrip.domain.savedcourse.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;

public interface TourReceiptRepository extends JpaRepository<TourReceipt, Long> {
}
