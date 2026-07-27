package live.lbtrip.domain.savedcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;
import live.lbtrip.domain.savedcourse.repository.TourReceiptRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class TourReceiptFinderTest {

    private static final Long RECEIPT_ID = 1L;
    private static final Long SAVED_COURSE_ID = 2L;

    @Mock
    private TourReceiptRepository tourReceiptRepository;

    @Mock
    private TourReceipt receipt;

    @InjectMocks
    private TourReceiptFinder tourReceiptFinder;

    @Nested
    class 상세_조회 {

        @Test
        void 저장_코스의_증빙을_조회한다() {
            when(tourReceiptRepository.findByIdAndSavedCourseId(RECEIPT_ID, SAVED_COURSE_ID))
                .thenReturn(Optional.of(receipt));

            TourReceipt result = tourReceiptFinder.findByIdAndSavedCourseId(
                RECEIPT_ID,
                SAVED_COURSE_ID
            );

            assertThat(result).isSameAs(receipt);
        }

        @Test
        void 저장_코스에_증빙이_없으면_예외를_던진다() {
            when(tourReceiptRepository.findByIdAndSavedCourseId(RECEIPT_ID, SAVED_COURSE_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                tourReceiptFinder.findByIdAndSavedCourseId(RECEIPT_ID, SAVED_COURSE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOUR_RECEIPT_NOT_FOUND);
        }
    }
}
