package live.lbtrip.domain.incentive.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class IncentiveTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 7, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 8, 31);

    @Nested
    class 생성 {

        @Test
        void 종료일이_있는_혜택을_생성한다() {
            Incentive incentive = Incentive.create("반값여행", "https://example.com", null, START_DATE, END_DATE);

            assertThat(incentive.getStartDate()).isEqualTo(START_DATE);
            assertThat(incentive.getEndDate()).isEqualTo(END_DATE);
        }

        @Test
        void 종료일이_없는_혜택을_생성한다() {
            Incentive incentive = Incentive.create("상시 혜택", "https://example.com", null, START_DATE, null);

            assertThat(incentive.getStartDate()).isEqualTo(START_DATE);
            assertThat(incentive.getEndDate()).isNull();
        }

        @Test
        void 시작일이_없으면_예외를_던진다() {
            assertThatThrownBy(() -> Incentive.create("반값여행", "https://example.com", null, null, END_DATE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCENTIVE_PERIOD);
        }

        @Test
        void 종료일이_시작일보다_빠르면_예외를_던진다() {
            assertThatThrownBy(() -> Incentive.create(
                "반값여행", "https://example.com", null, START_DATE, START_DATE.minusDays(1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCENTIVE_PERIOD);
        }
    }

    @Nested
    class 수정 {

        @Test
        void 유효하지_않은_기간으로_수정하면_기존_혜택_정보를_변경하지_않는다() {
            Incentive incentive = Incentive.create("기존 혜택", "https://before.example.com", "기존 설명", START_DATE, END_DATE);

            assertThatThrownBy(() -> incentive.update(
                "변경 혜택", "https://after.example.com", "변경 설명", END_DATE, START_DATE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCENTIVE_PERIOD);

            assertThat(incentive.getTitle()).isEqualTo("기존 혜택");
            assertThat(incentive.getUrl()).isEqualTo("https://before.example.com");
            assertThat(incentive.getDescription()).isEqualTo("기존 설명");
            assertThat(incentive.getStartDate()).isEqualTo(START_DATE);
            assertThat(incentive.getEndDate()).isEqualTo(END_DATE);
        }
    }
}
