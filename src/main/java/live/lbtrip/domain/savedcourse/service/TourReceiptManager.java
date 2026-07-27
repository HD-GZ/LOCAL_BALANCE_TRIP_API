package live.lbtrip.domain.savedcourse.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.service.ImageService;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.domain.savedcourse.repository.TourReceiptRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourReceiptManager {

    private final TourReceiptRepository tourReceiptRepository;
    private final ImageService imageService;

    public TourReceipt add(
        SavedCourse savedCourse,
        String merchantName,
        int amount,
        LocalDate paidDate,
        Image image
    ) {
        TourReceipt receipt = TourReceipt.create(
            savedCourse,
            merchantName,
            amount,
            paidDate,
            image
        );
        return tourReceiptRepository.save(receipt);
    }

    public void delete(TourReceipt receipt) {
        tourReceiptRepository.delete(receipt);
        imageService.delete(receipt.getImage());
    }
}
