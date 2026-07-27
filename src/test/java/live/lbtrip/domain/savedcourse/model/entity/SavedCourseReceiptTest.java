package live.lbtrip.domain.savedcourse.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.user.model.User;

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
