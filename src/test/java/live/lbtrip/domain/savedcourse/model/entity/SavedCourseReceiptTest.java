package live.lbtrip.domain.savedcourse.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class SavedCourseReceiptTest {

    @Nested
    class 증빙 {

        @Test
        void 증빙을_생성하면_저장_코스에_연결한다() {
            SavedCourse savedCourse = savedCourse();

            TourReceipt receipt = receipt(savedCourse, 10000);

            assertThat(savedCourse.getReceipts()).containsExactly(receipt);
        }

        @Test
        void 연결된_증빙_금액의_합계를_계산한다() {
            SavedCourse savedCourse = savedCourse();
            receipt(savedCourse, 10000);
            receipt(savedCourse, 20000);

            int result = savedCourse.calculateTotalReceiptAmount();

            assertThat(result).isEqualTo(30000);
        }

        @Test
        void 연결된_증빙이_없으면_합계는_0이다() {
            assertThat(savedCourse().calculateTotalReceiptAmount()).isZero();
        }

        @Test
        void 식별자로_연결된_증빙을_조회한다() {
            SavedCourse savedCourse = savedCourse();
            TourReceipt receipt = mock(TourReceipt.class);
            when(receipt.getId()).thenReturn(1L);
            savedCourse.addReceipt(receipt);

            TourReceipt result = savedCourse.findReceiptById(1L);

            assertThat(result).isSameAs(receipt);
        }

        @Test
        void 식별자에_해당하는_증빙이_없으면_예외를_던진다() {
            assertThatThrownBy(() -> savedCourse().findReceiptById(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOUR_RECEIPT_NOT_FOUND);
        }
    }

    private SavedCourse savedCourse() {
        return SavedCourse.create(
            mock(User.class),
            1L,
            "코스명",
            "담양",
            "추천 이유",
            null,
            "46",
            "710"
        );
    }

    private TourReceipt receipt(SavedCourse savedCourse, int amount) {
        return TourReceipt.create(
            savedCourse,
            "가맹점",
            amount,
            LocalDate.of(2026, 7, 17),
            mock(Image.class)
        );
    }
}
