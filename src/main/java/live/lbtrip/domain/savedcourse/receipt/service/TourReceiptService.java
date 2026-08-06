package live.lbtrip.domain.savedcourse.receipt.service;

import static live.lbtrip.global.storage.enums.ImageDirectory.RECEIPT;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.model.vo.ImageRegistration;
import live.lbtrip.domain.image.service.ImageService;
import live.lbtrip.domain.savedcourse.receipt.dto.request.TourReceiptCreateRequest;
import live.lbtrip.domain.savedcourse.receipt.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptDownloadUrlResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptResponse;
import live.lbtrip.domain.savedcourse.receipt.model.vo.ReceiptOcrResult;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.course.service.SavedCourseFinder;
import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.global.storage.vo.PresignedUrl;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourReceiptService {

    private final SavedCourseFinder savedCourseFinder;
    private final TourReceiptManager tourReceiptManager;
    private final ReceiptOcrExtractor receiptOcrExtractor;
    private final ImageService imageService;

    @Transactional
    public ReceiptScanResponse scan(Long userId, Long savedCourseId, MultipartFile image) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        ImageRegistration registration = imageService.register(userId, RECEIPT, image);
        ReceiptOcrResult result = receiptOcrExtractor.extract(registration.validatedImage());

        return ReceiptScanResponse.of(
            registration.image().getId(),
            imageService.getViewUrl(registration.image()),
            result
        );
    }

    @Transactional
    public TourReceiptResponse create(Long userId, Long savedCourseId, TourReceiptCreateRequest request) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        Image image = imageService.claim(request.imageId(), userId, RECEIPT);

        TourReceipt receipt = tourReceiptManager.add(
            savedCourse,
            StringNormalizer.trim(request.merchantName()),
            request.amount(),
            request.paidDate(),
            image
        );

        return TourReceiptResponse.from(receipt, imageService.getViewUrl(image));
    }

    public TourReceiptListResponse getReceipts(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        return TourReceiptListResponse.from(savedCourse);
    }

    public TourReceiptResponse getReceipt(Long userId, Long savedCourseId, Long receiptId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        TourReceipt receipt = savedCourse.findReceiptById(receiptId);
        return TourReceiptResponse.from(receipt, imageService.getViewUrl(receipt.getImage()));
    }

    public TourReceiptDownloadUrlResponse getDownloadUrl(Long userId, Long savedCourseId, Long receiptId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        TourReceipt receipt = savedCourse.findReceiptById(receiptId);
        PresignedUrl presignedUrl = imageService.getDownloadUrl(receipt.getImage(), downloadFilename(receipt));
        return TourReceiptDownloadUrlResponse.from(presignedUrl);
    }

    private String downloadFilename(TourReceipt receipt) {
        String storageKey = receipt.getImage().getStorageKey();
        String extension = storageKey.substring(storageKey.lastIndexOf('.') + 1);
        return "receipt_%d.%s".formatted(receipt.getId(), extension);
    }

    @Transactional
    public void delete(Long userId, Long savedCourseId, Long receiptId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        TourReceipt receipt = savedCourse.findReceiptById(receiptId);
        tourReceiptManager.delete(receipt);
    }
}
