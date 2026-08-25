package live.lbtrip.domain.tourism.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.tourism.client.dto.TourEventItem;
import live.lbtrip.global.config.TourApiProperties;

@ExtendWith(MockitoExtension.class)
class TourApiClientTest {

    private static final String KOR = "http://kor";
    private static final String ENG = "http://eng";

    @Mock
    private PublicDataClient publicDataClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TourApiClient client() {
        return new TourApiClient(publicDataClient,
            new TourApiProperties(KOR, ENG, "http://odii", "http://datalab", "key", "ETC", "app", Duration.ofSeconds(1)));
    }

    private JsonNode body(String json) throws Exception {
        return objectMapper.readTree(json);
    }

    @Nested
    class 행사_조회 {

        @Test
        void 검색_기간과_법정동_코드로_searchFestival2를_호출하고_항목을_매핑한다() throws Exception {
            JsonNode body = body("""
                {"items":{"item":[{"contentid":"1","title":"담양 대나무축제","eventstartdate":"20260901",
                "eventenddate":"20260905","firstimage":"https://img/1.jpg","mapx":"126.98","mapy":"35.32",
                "addr1":"전남 담양군 죽녹원로 119","tel":"061-380-3150"}]},"totalCount":1}
                """);
            when(publicDataClient.get(eq(KOR), eq("/searchFestival2"), any())).thenReturn(body);
            when(publicDataClient.items(body)).thenReturn(body.path("items").path("item"));

            List<TourEventItem> events = client().fetchFestivals(
                "46", "710", LocalDate.of(2026, 8, 25), LocalDate.of(2026, 10, 24), Locale.KOREAN);

            ArgumentCaptor<UnaryOperator<UriBuilder>> captor = ArgumentCaptor.captor();
            org.mockito.Mockito.verify(publicDataClient).get(eq(KOR), eq("/searchFestival2"), captor.capture());
            URI uri = captor.getValue().apply(UriComponentsBuilder.fromUriString(KOR)).build();
            assertThat(uri.getQuery())
                .contains("eventStartDate=20260825")
                .contains("eventEndDate=20261024")
                .contains("lDongRegnCd=46")
                .contains("lDongSignguCd=710")
                .contains("arrange=O");
            assertThat(events).hasSize(1);
            TourEventItem event = events.getFirst();
            assertThat(event.contentId()).isEqualTo("1");
            assertThat(event.title()).isEqualTo("담양 대나무축제");
            assertThat(event.eventStart()).isEqualTo(LocalDate.of(2026, 9, 1));
            assertThat(event.eventEnd()).isEqualTo(LocalDate.of(2026, 9, 5));
            assertThat(event.imageUrl()).isEqualTo("https://img/1.jpg");
            assertThat(event.longitude()).isEqualTo(126.98);
            assertThat(event.latitude()).isEqualTo(35.32);
            assertThat(event.address()).isEqualTo("전남 담양군 죽녹원로 119");
            assertThat(event.tel()).isEqualTo("061-380-3150");
        }

        @Test
        void 영문_로케일은_영문_기본_URL로_호출한다() throws Exception {
            JsonNode body = body("{\"items\":\"\",\"totalCount\":0}");
            when(publicDataClient.get(eq(ENG), eq("/searchFestival2"), any())).thenReturn(body);
            when(publicDataClient.items(body)).thenReturn(objectMapper.createArrayNode());

            List<TourEventItem> events = client().fetchFestivals(
                "46", "710", LocalDate.of(2026, 8, 25), LocalDate.of(2026, 10, 24), Locale.ENGLISH);

            assertThat(events).isEmpty();
        }

        @Test
        void 날짜가_비어_있는_항목은_건너뛴다() throws Exception {
            JsonNode body = body("""
                {"items":{"item":[{"contentid":"1","title":"없음","eventstartdate":"","eventenddate":"20260905"},
                {"contentid":"2","title":"정상","eventstartdate":"20260901","eventenddate":"20260905"}]}}
                """);
            when(publicDataClient.get(eq(KOR), eq("/searchFestival2"), any())).thenReturn(body);
            when(publicDataClient.items(body)).thenReturn(body.path("items").path("item"));

            List<TourEventItem> events = client().fetchFestivals(
                "46", "710", LocalDate.of(2026, 8, 25), LocalDate.of(2026, 10, 24), Locale.KOREAN);

            assertThat(events).extracting(TourEventItem::contentId).containsExactly("2");
            assertThat(events.getFirst().imageUrl()).isNull();
        }
    }
}
