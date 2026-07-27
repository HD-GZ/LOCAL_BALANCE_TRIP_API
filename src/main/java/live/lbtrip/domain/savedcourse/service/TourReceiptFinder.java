package live.lbtrip.domain.savedcourse.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.domain.savedcourse.repository.TourReceiptRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourReceiptFinder {

    private final TourReceiptRepository tourReceiptRepository;

    public TourReceipt findByIdAndSavedCourseId(Long id, Long savedCourseId) {
        return tourReceiptRepository.findByIdAndSavedCourseId(id, savedCourseId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_RECEIPT_NOT_FOUND));
    }
}
