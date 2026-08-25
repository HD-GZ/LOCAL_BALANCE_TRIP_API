package live.lbtrip.domain.tourism.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.TourEventFixture;

class TourEventTest {

    @Nested
    class 생성과_갱신 {

        @Test
        void 로케일과_콘텐츠_ID_기간을_가진_행사를_생성한다() {
            TourEvent event = TourEventFixture.event("100", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5));

            assertThat(event.getLocale()).isEqualTo(Locale.KOREAN);
            assertThat(event.getContentId()).isEqualTo("100");
            assertThat(event.getTitle()).isEqualTo(TourEventFixture.TITLE);
            assertThat(event.getEventStart()).isEqualTo(LocalDate.of(2026, 9, 1));
            assertThat(event.getEventEnd()).isEqualTo(LocalDate.of(2026, 9, 5));
        }

        @Test
        void 갱신하면_제목_이미지_좌표_주소_기간이_바뀐다() {
            TourEvent event = TourEventFixture.event("100", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5));

            event.update("새 제목", "https://img/new.jpg", 35.0, 127.0, "새 주소",
                LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 6));

            assertThat(event.getTitle()).isEqualTo("새 제목");
            assertThat(event.getImageUrl()).isEqualTo("https://img/new.jpg");
            assertThat(event.getLatitude()).isEqualTo(35.0);
            assertThat(event.getLongitude()).isEqualTo(127.0);
            assertThat(event.getAddress()).isEqualTo("새 주소");
            assertThat(event.getEventStart()).isEqualTo(LocalDate.of(2026, 9, 2));
            assertThat(event.getEventEnd()).isEqualTo(LocalDate.of(2026, 9, 6));
        }
    }
}
