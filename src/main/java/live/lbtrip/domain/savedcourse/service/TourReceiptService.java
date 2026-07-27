package live.lbtrip.domain.savedcourse.service;

import static live.lbtrip.global.storage.ImageDirectory.RECEIPT;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.service.ImageService;
import live.lbtrip.domain.savedcourse.dto.request.TourReceiptCreateRequest;
import live.lbtrip.domain.savedcourse.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptResponse;
import live.lbtrip.domain.savedcourse.model.ReceiptOcrResult;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.domain.savedcourse.repository.TourReceiptRepository;
import live.lbtrip.global.storage.ImageFileValidator;
import live.lbtrip.global.storage.ImageStorage;
import live.lbtrip.global.storage.ValidatedImage;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourReceiptService {

    private final SavedCourseFinder savedCourseFinder;
    private final TourReceiptFinder tourReceiptFinder;
    private final TourReceiptRepository tourReceiptRepository;
    private final ImageStorage imageStorage;
    private final ImageFileValidator imageFileValidator;
    private final ReceiptOcrExtractor receiptOcrExtractor;
    private final ImageService imageService;

    public ReceiptScanResponse scan(Long userId, Long savedCourseId, MultipartFile image) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        ValidatedImage validatedImage = imageFileValidator.validate(image);
        String imageKey = imageStorage.store(validatedImage, RECEIPT);
        Image registeredImage = imageService.register(
            userId,
            RECEIPT,
            imageKey,
            validatedImage.mediaType().toString(),
            validatedImage.size()
        );
        ReceiptOcrResult result = receiptOcrExtractor.extract(validatedImage);

        return ReceiptScanResponse.of(
            registeredImage.getId(),
            imageStorage.publicUrl(imageKey),
            result
        );
    }

    @Transactional
    public TourReceiptResponse create(Long userId, Long savedCourseId, TourReceiptCreateRequest request) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        Image image = imageService.claim(request.imageId(), userId, RECEIPT);

        TourReceipt receipt = TourReceipt.create(
            savedCourse,
            StringNormalizer.trim(request.merchantName()),
            request.amount(),
            request.paidDate(),
            image
        );
        tourReceiptRepository.save(receipt);

        return TourReceiptResponse.from(receipt, imageStorage.publicUrl(receipt.getImage().getStorageKey()));
    }

    public TourReceiptListResponse getReceipts(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        return TourReceiptListResponse.from(savedCourse);
    }

    public TourReceiptResponse getReceipt(Long userId, Long savedCourseId, Long receiptId) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        TourReceipt receipt = tourReceiptFinder.findByIdAndSavedCourseId(receiptId, savedCourseId);
        return TourReceiptResponse.from(receipt, imageStorage.publicUrl(receipt.getImage().getStorageKey()));
    }

    @Transactional
    public void delete(Long userId, Long savedCourseId, Long receiptId) {
        savedCourseFinder.findByIdAndUserId(savedCourseId, userId);

        TourReceipt receipt = tourReceiptFinder.findByIdAndSavedCourseId(receiptId, savedCourseId);
        String imageKey = receipt.getImage().getStorageKey();
        tourReceiptRepository.delete(receipt);
        imageStorage.delete(imageKey);
    }
}
