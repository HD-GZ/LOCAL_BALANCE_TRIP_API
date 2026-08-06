package live.lbtrip.domain.savedcourse.receipt.service;

import static live.lbtrip.global.storage.enums.ImageDirectory.RECEIPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.model.vo.ImageRegistration;
import live.lbtrip.domain.image.service.ImageService;
import live.lbtrip.domain.savedcourse.receipt.dto.request.TourReceiptCreateRequest;
import live.lbtrip.domain.savedcourse.receipt.dto.request.TourReceiptUpdateRequest;
import live.lbtrip.domain.savedcourse.receipt.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptDownloadUrlResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptResponse;
import live.lbtrip.domain.savedcourse.receipt.model.vo.ReceiptOcrResult;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.course.service.SavedCourseFinder;
import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.vo.PresignedUrl;
import live.lbtrip.global.storage.vo.ValidatedImage;

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
    private TourReceiptManager tourReceiptManager;

    @Mock
    private ReceiptOcrExtractor receiptOcrExtractor;

    @Mock
    private ImageService imageService;

    @Mock
    private SavedCourse savedCourse;

    @Mock
    private Image image;

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
            when(imageService.register(USER_ID, RECEIPT, multipartFile))
                .thenReturn(ImageRegistration.of(image, validatedImage));
            when(image.getId()).thenReturn(IMAGE_ID);
            when(receiptOcrExtractor.extract(validatedImage)).thenReturn(ReceiptOcrResult.empty());
            when(imageService.getViewUrl(image)).thenReturn(IMAGE_URL);

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

            verify(imageService, never()).register(any(), any(), any());
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
            when(imageService.claim(IMAGE_ID, USER_ID, RECEIPT))
                .thenReturn(image);
            TourReceipt receipt = TourReceipt.create(
                savedCourse,
                "국수거리 노포",
                18000,
                PAID_DATE,
                image
            );
            when(tourReceiptManager.add(
                savedCourse,
                "국수거리 노포",
                18000,
                PAID_DATE,
                image
            )).thenReturn(receipt);
            when(imageService.getViewUrl(image)).thenReturn(IMAGE_URL);

            TourReceiptResponse result = tourReceiptService.create(USER_ID, SAVED_COURSE_ID, request);

            verify(tourReceiptManager).add(
                savedCourse,
                "국수거리 노포",
                18000,
                PAID_DATE,
                image
            );
            assertThat(result.amount()).isEqualTo(18000);
            assertThat(result.imageUrl()).isEqualTo(IMAGE_URL);
        }
    }

    @Nested
    class 수정 {

        @Test
        void 가맹점명을_정규화해_증빙을_수정한다() {
            TourReceipt receipt = TourReceipt.create(
                savedCourse,
                "이전 가맹점",
                10000,
                LocalDate.of(2026, 7, 1),
                image
            );
            TourReceiptUpdateRequest request = TourReceiptUpdateRequest.of(
                "  국수거리 노포  ",
                18000,
                PAID_DATE
            );
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.findReceiptById(RECEIPT_ID)).thenReturn(receipt);
            when(imageService.getViewUrl(image)).thenReturn(IMAGE_URL);

            TourReceiptResponse result = tourReceiptService.update(
                USER_ID, SAVED_COURSE_ID, RECEIPT_ID, request
            );

            assertThat(receipt.getMerchantName()).isEqualTo("국수거리 노포");
            assertThat(receipt.getAmount()).isEqualTo(18000);
            assertThat(receipt.getPaidDate()).isEqualTo(PAID_DATE);
            assertThat(result.merchantName()).isEqualTo("국수거리 노포");
            assertThat(result.amount()).isEqualTo(18000);
            assertThat(result.paidDate()).isEqualTo(PAID_DATE);
            assertThat(result.imageUrl()).isEqualTo(IMAGE_URL);
        }

        @Test
        void 저장_코스에_없는_증빙이면_수정하지_않는다() {
            TourReceiptUpdateRequest request = TourReceiptUpdateRequest.of(
                "국수거리 노포",
                18000,
                PAID_DATE
            );
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.findReceiptById(RECEIPT_ID))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_RECEIPT_NOT_FOUND));

            assertErrorCode(
                () -> tourReceiptService.update(USER_ID, SAVED_COURSE_ID, RECEIPT_ID, request),
                ErrorCode.TOUR_RECEIPT_NOT_FOUND
            );

            verify(imageService, never()).getViewUrl(any());
        }
    }

    @Nested
    class 다운로드_URL {

        @Test
        void 만료_시각과_함께_다운로드_URL을_발급한다() {
            String downloadUrl = IMAGE_URL + "?X-Amz-Signature=abc";
            LocalDateTime expiresAt = LocalDateTime.of(2026, 8, 6, 19, 10, 30);
            TourReceipt receipt = mock(TourReceipt.class);
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.findReceiptById(RECEIPT_ID)).thenReturn(receipt);
            when(receipt.getId()).thenReturn(RECEIPT_ID);
            when(receipt.getImage()).thenReturn(image);
            when(image.getStorageKey()).thenReturn(IMAGE_KEY);
            when(imageService.getDownloadUrl(image, "receipt_4.jpg"))
                .thenReturn(PresignedUrl.of(downloadUrl, expiresAt));

            TourReceiptDownloadUrlResponse result =
                tourReceiptService.getDownloadUrl(USER_ID, SAVED_COURSE_ID, RECEIPT_ID);

            assertThat(result.downloadUrl()).isEqualTo(downloadUrl);
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
        }

        @Test
        void 저장_코스에_없는_증빙이면_URL을_발급하지_않는다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.findReceiptById(RECEIPT_ID))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_RECEIPT_NOT_FOUND));

            assertErrorCode(
                () -> tourReceiptService.getDownloadUrl(USER_ID, SAVED_COURSE_ID, RECEIPT_ID),
                ErrorCode.TOUR_RECEIPT_NOT_FOUND
            );

            verify(imageService, never()).getDownloadUrl(any(), any());
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
            when(savedCourse.getReceipts()).thenReturn(List.of(first, second));
            when(savedCourse.calculateTotalReceiptAmount()).thenReturn(30000);

            TourReceiptListResponse result = tourReceiptService.getReceipts(USER_ID, SAVED_COURSE_ID);

            assertThat(result.totalAmount()).isEqualTo(30000);
            assertThat(result.receipts()).hasSize(2);
        }

        @Test
        void 저장_코스에_없는_증빙이면_예외를_던진다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.findReceiptById(RECEIPT_ID))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_RECEIPT_NOT_FOUND));

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
                image
            );
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.findReceiptById(RECEIPT_ID))
                .thenReturn(receipt);

            tourReceiptService.delete(USER_ID, SAVED_COURSE_ID, RECEIPT_ID);

            verify(tourReceiptManager).delete(receipt);
        }
    }

    private TourReceipt receipt(int amount) {
        return TourReceipt.create(
            savedCourse,
            "가맹점",
            amount,
            PAID_DATE,
            image
        );
    }

    private void assertErrorCode(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(errorCode);
    }
}
