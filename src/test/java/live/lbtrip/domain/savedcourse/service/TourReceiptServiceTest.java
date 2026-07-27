package live.lbtrip.domain.savedcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.domain.savedcourse.dto.request.TourReceiptCreateRequest;
import live.lbtrip.domain.savedcourse.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptResponse;
import live.lbtrip.domain.savedcourse.model.ReceiptOcrResult;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.StoredImage;
import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.domain.savedcourse.repository.TourReceiptRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.ImageFileValidator;
import live.lbtrip.global.storage.ImageStorage;
import live.lbtrip.global.storage.ValidatedImage;

@ExtendWith(MockitoExtension.class)
class TourReceiptServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SAVED_COURSE_ID = 2L;
    private static final Long IMAGE_ID = 3L;
    private static final Long RECEIPT_ID = 4L;
    private static final String IMAGE_KEY = "receipts/2026/07/test.jpg";
    private static final String IMAGE_URL = "https://images.example.com/" + IMAGE_KEY;
    private static final LocalDate PAID_DATE = LocalDate.of(2026, 7, 17);

    @Mock
    private SavedCourseFinder savedCourseFinder;

    @Mock
    private TourReceiptRepository tourReceiptRepository;

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private ImageFileValidator imageFileValidator;

    @Mock
    private ReceiptOcrExtractor receiptOcrExtractor;

    @Mock
    private StoredImageService storedImageService;

    @Mock
    private SavedCourse savedCourse;

    @Mock
    private StoredImage storedImage;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private TourReceiptService tourReceiptService;

    @Nested
    class 스캔 {

        @Test
        void OCR_결과가_없어도_이미지_정보를_반환한다() {
            ValidatedImage validatedImage = ValidatedImage.of(
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
                "jpg",
                MediaType.IMAGE_JPEG
            );
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(imageFileValidator.validate(multipartFile)).thenReturn(validatedImage);
            when(imageStorage.store(validatedImage, "receipts")).thenReturn(IMAGE_KEY);
            when(storedImageService.registerReceipt(
                savedCourse,
                IMAGE_KEY,
                MediaType.IMAGE_JPEG_VALUE,
                validatedImage.size()
            )).thenReturn(storedImage);
            when(storedImage.getId()).thenReturn(IMAGE_ID);
            when(receiptOcrExtractor.extract(validatedImage)).thenReturn(ReceiptOcrResult.empty());
            when(imageStorage.publicUrl(IMAGE_KEY)).thenReturn(IMAGE_URL);

            ReceiptScanResponse result = tourReceiptService.scan(USER_ID, SAVED_COURSE_ID, multipartFile);

            assertThat(result.imageId()).isEqualTo(IMAGE_ID);
            assertThat(result.imageUrl()).isEqualTo(IMAGE_URL);
            assertThat(result.merchantName()).isNull();
            assertThat(result.amount()).isNull();
            assertThat(result.paidDate()).isNull();
        }

        @Test
        void 저장_코스가_없으면_이미지를_검증하거나_저장하지_않는다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenThrow(BusinessException.of(ErrorCode.SAVED_COURSE_NOT_FOUND));

            assertErrorCode(
                () -> tourReceiptService.scan(USER_ID, SAVED_COURSE_ID, multipartFile),
                ErrorCode.SAVED_COURSE_NOT_FOUND
            );

            verify(imageFileValidator, never()).validate(any());
            verify(imageStorage, never()).store(any(), any());
        }
    }

    @Nested
    class 등록 {

        @Test
        void 사용자가_직접_입력한_필드로_증빙을_등록한다() {
            TourReceiptCreateRequest request = TourReceiptCreateRequest.of(
                IMAGE_ID,
                "  국수거리 노포  ",
                18000,
                PAID_DATE
            );
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(storedImageService.claimReceipt(IMAGE_ID, SAVED_COURSE_ID))
                .thenReturn(storedImage);
            when(storedImage.getStorageKey()).thenReturn(IMAGE_KEY);
            when(imageStorage.publicUrl(IMAGE_KEY)).thenReturn(IMAGE_URL);

            TourReceiptResponse result = tourReceiptService.create(USER_ID, SAVED_COURSE_ID, request);

            ArgumentCaptor<TourReceipt> receiptCaptor = ArgumentCaptor.forClass(TourReceipt.class);
            verify(tourReceiptRepository).save(receiptCaptor.capture());
            TourReceipt savedReceipt = receiptCaptor.getValue();
            assertThat(savedReceipt.getMerchantName()).isEqualTo("국수거리 노포");
            assertThat(savedReceipt.getImage()).isSameAs(storedImage);
            assertThat(result.amount()).isEqualTo(18000);
            assertThat(result.imageUrl()).isEqualTo(IMAGE_URL);
        }
    }

    @Nested
    class 조회와_삭제 {

        @Test
        void 증빙_금액_합계와_목록을_반환한다() {
            TourReceipt first = receipt(10000);
            TourReceipt second = receipt(20000);
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(tourReceiptRepository.findAllBySavedCourseIdOrderByIdDesc(SAVED_COURSE_ID))
                .thenReturn(List.of(first, second));

            TourReceiptListResponse result = tourReceiptService.getReceipts(USER_ID, SAVED_COURSE_ID);

            assertThat(result.totalAmount()).isEqualTo(30000);
            assertThat(result.receipts()).hasSize(2);
        }

        @Test
        void 저장_코스에_없는_증빙이면_예외를_던진다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(tourReceiptRepository.findByIdAndSavedCourseId(RECEIPT_ID, SAVED_COURSE_ID))
                .thenReturn(Optional.empty());

            assertErrorCode(
                () -> tourReceiptService.getReceipt(USER_ID, SAVED_COURSE_ID, RECEIPT_ID),
                ErrorCode.TOUR_RECEIPT_NOT_FOUND
            );
        }

        @Test
        void 증빙과_원본_이미지를_삭제한다() {
            TourReceipt receipt = TourReceipt.create(
                savedCourse,
                "국수거리 노포",
                18000,
                PAID_DATE,
                storedImage
            );
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(tourReceiptRepository.findByIdAndSavedCourseId(RECEIPT_ID, SAVED_COURSE_ID))
                .thenReturn(Optional.of(receipt));
            when(storedImage.getStorageKey()).thenReturn(IMAGE_KEY);

            tourReceiptService.delete(USER_ID, SAVED_COURSE_ID, RECEIPT_ID);

            verify(tourReceiptRepository).delete(receipt);
            verify(imageStorage).delete(IMAGE_KEY);
        }
    }

    private TourReceipt receipt(int amount) {
        return TourReceipt.create(
            savedCourse,
            "가맹점",
            amount,
            PAID_DATE,
            storedImage
        );
    }

    private void assertErrorCode(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(errorCode);
    }
}
