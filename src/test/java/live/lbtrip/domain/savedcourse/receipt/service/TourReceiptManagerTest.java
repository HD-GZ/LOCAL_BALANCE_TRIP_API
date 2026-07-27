package live.lbtrip.domain.savedcourse.receipt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.service.ImageService;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.domain.savedcourse.receipt.repository.TourReceiptRepository;

@ExtendWith(MockitoExtension.class)
class TourReceiptManagerTest {

    private static final LocalDate PAID_DATE = LocalDate.of(2026, 7, 17);

    @Mock
    private TourReceiptRepository tourReceiptRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private SavedCourse savedCourse;

    @Mock
    private Image image;

    @InjectMocks
    private TourReceiptManager tourReceiptManager;

    @Test
    void 환급_증빙을_추가한다() {
        when(tourReceiptRepository.save(any(TourReceipt.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        TourReceipt result = tourReceiptManager.add(
            savedCourse,
            "국수거리 노포",
            18000,
            PAID_DATE,
            image
        );

        assertThat(result.getSavedCourse()).isSameAs(savedCourse);
        assertThat(result.getMerchantName()).isEqualTo("국수거리 노포");
        assertThat(result.getAmount()).isEqualTo(18000);
        assertThat(result.getPaidDate()).isEqualTo(PAID_DATE);
        assertThat(result.getImage()).isSameAs(image);
        verify(tourReceiptRepository).save(result);
    }

    @Test
    void 환급_증빙과_연결된_이미지를_삭제한다() {
        TourReceipt receipt = TourReceipt.create(
            savedCourse,
            "국수거리 노포",
            18000,
            PAID_DATE,
            image
        );

        tourReceiptManager.delete(receipt);

        verify(tourReceiptRepository).delete(receipt);
        verify(imageService).delete(image);
    }
}
