package live.lbtrip.domain.savedcourse.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.domain.savedcourse.dto.request.TourReceiptCreateRequest;
import live.lbtrip.domain.savedcourse.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptResponse;
import live.lbtrip.domain.savedcourse.model.ReceiptOcrResult;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.domain.savedcourse.repository.TourReceiptRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.ImageStorage;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourReceiptService {

    private static final String RECEIPT_IMAGE_DIRECTORY = "receipts";

    private final SavedCourseFinder savedCourseFinder;
    private final TourReceiptRepository tourReceiptRepository;
    private final ImageStorage imageStorage;
    private final ReceiptOcrExtractor receiptOcrExtractor;

    public ReceiptScanResponse scan(Long userId, Long savedCourseId, MultipartFile image) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        String imageKey = imageStorage.store(image, RECEIPT_IMAGE_DIRECTORY);
        ReceiptOcrResult result = receiptOcrExtractor.extract(image);

        return ReceiptScanResponse.of(imageKey, imageStorage.publicUrl(imageKey), result);
    }

    @Transactional
    public TourReceiptResponse create(Long userId, Long savedCourseId, TourReceiptCreateRequest request) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        TourReceipt receipt = TourReceipt.create(
            savedCourse,
            StringNormalizer.trim(request.merchantName()),
            request.amount(),
            request.paidDate(),
            StringNormalizer.trim(request.imageKey())
        );
        tourReceiptRepository.save(receipt);

        return TourReceiptResponse.of(receipt, imageStorage.publicUrl(receipt.getImageKey()));
    }

    public TourReceiptListResponse getReceipts(Long userId, Long savedCourseId) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        List<TourReceipt> receipts = tourReceiptRepository.findAllBySavedCourseIdOrderByIdDesc(savedCourseId);
        return TourReceiptListResponse.from(receipts);
    }

    public TourReceiptResponse getReceipt(Long userId, Long savedCourseId, Long receiptId) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        TourReceipt receipt = findReceipt(receiptId, savedCourseId);
        return TourReceiptResponse.of(receipt, imageStorage.publicUrl(receipt.getImageKey()));
    }

    @Transactional
    public void delete(Long userId, Long savedCourseId, Long receiptId) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        TourReceipt receipt = findReceipt(receiptId, savedCourseId);
        String imageKey = receipt.getImageKey();
        tourReceiptRepository.delete(receipt);
        imageStorage.delete(imageKey);
    }

    private TourReceipt findReceipt(Long receiptId, Long savedCourseId) {
        return tourReceiptRepository.findByIdAndSavedCourseId(receiptId, savedCourseId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_RECEIPT_NOT_FOUND));
    }
}
