package live.lbtrip.domain.tourism.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.tourism.client.dto.DurunubiCourseItem;
import live.lbtrip.global.config.TourApiProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class DurunubiClientTest {

    private static final String BASE_URL = "http://localhost/Durunubi";

    @Mock
    private PublicDataClient publicDataClient;

    @Mock
    private TourApiProperties properties;

    @InjectMocks
    private DurunubiClient durunubiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    class 코스_목록_조회 {

        @Test
        void 문자열_숫자_필드를_관대하게_파싱한다() throws Exception {
            JsonNode body = objectMapper.readTree("""
                {"items":{"item":[{
                    "crsIdx":"T_CRS_MNG0000000123","crsKorNm":"화진포 둘레길","crsDstnc":"12.5",
                    "crsTotlRqrdHour":"240","crsLevel":"2","sigun":"강원도 고성군",
                    "gpxpath":"https://example.com/a.gpx","brdDiv":"DNWW","routeIdx":"T_ROUTE_MNG0000000001",
                    "crsSummary":"바다를 따라 걷는 길"
                }]}}
                """);
            when(properties.durunubiBaseUrl()).thenReturn(BASE_URL);
            when(publicDataClient.get(eq(BASE_URL), eq("/courseList"), any())).thenReturn(body);
            when(publicDataClient.items(body)).thenReturn(body.path("items").path("item"));

            List<DurunubiCourseItem> items = durunubiClient.fetchCourses(1);

            assertThat(items).hasSize(1);
            DurunubiCourseItem item = items.getFirst();
            assertThat(item.crsIdx()).isEqualTo("T_CRS_MNG0000000123");
            assertThat(item.crsKorNm()).isEqualTo("화진포 둘레길");
            assertThat(item.crsDstnc()).isEqualByComparingTo("12.5");
            assertThat(item.crsTotlRqrdHour()).isEqualTo(240);
            assertThat(item.crsLevel()).isEqualTo(2);
            assertThat(item.sigun()).isEqualTo("강원도 고성군");
            assertThat(item.gpxpath()).isEqualTo("https://example.com/a.gpx");
            assertThat(item.brdDiv()).isEqualTo("DNWW");
            assertThat(item.routeIdx()).isEqualTo("T_ROUTE_MNG0000000001");
            assertThat(item.crsSummary()).isEqualTo("바다를 따라 걷는 길");
        }

        @Test
        void 잘못된_숫자와_빈_문자열은_null로_파싱한다() throws Exception {
            JsonNode body = objectMapper.readTree("""
                {"items":{"item":[{
                    "crsIdx":"T_CRS_MNG0000000124","crsKorNm":"길","crsDstnc":"abc",
                    "crsTotlRqrdHour":"","crsLevel":"","sigun":"","crsSummary":""
                }]}}
                """);
            when(properties.durunubiBaseUrl()).thenReturn(BASE_URL);
            when(publicDataClient.get(eq(BASE_URL), eq("/courseList"), any())).thenReturn(body);
            when(publicDataClient.items(body)).thenReturn(body.path("items").path("item"));

            DurunubiCourseItem item = durunubiClient.fetchCourses(1).getFirst();

            assertThat(item.crsDstnc()).isNull();
            assertThat(item.crsTotlRqrdHour()).isNull();
            assertThat(item.crsLevel()).isNull();
            assertThat(item.sigun()).isNull();
            assertThat(item.crsSummary()).isNull();
        }

        @Test
        void 코스_식별자가_없는_항목은_제외한다() throws Exception {
            JsonNode body = objectMapper.readTree("""
                {"items":{"item":[{"crsKorNm":"이름만"},{"crsIdx":"X","crsKorNm":"정상"}]}}
                """);
            when(properties.durunubiBaseUrl()).thenReturn(BASE_URL);
            when(publicDataClient.get(eq(BASE_URL), eq("/courseList"), any())).thenReturn(body);
            when(publicDataClient.items(body)).thenReturn(body.path("items").path("item"));

            List<DurunubiCourseItem> items = durunubiClient.fetchCourses(1);

            assertThat(items).extracting(DurunubiCourseItem::crsIdx).containsExactly("X");
        }

        @Test
        void 조회_실패_시_빈_목록을_반환한다() {
            when(properties.durunubiBaseUrl()).thenReturn(BASE_URL);
            when(publicDataClient.get(eq(BASE_URL), eq("/courseList"), any()))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE));

            assertThat(durunubiClient.fetchCourses(1)).isEmpty();
        }

        @Test
        void 한도_초과는_그대로_전파한다() {
            BusinessException quotaExceeded = BusinessException.of(ErrorCode.TOUR_API_QUOTA_EXCEEDED);
            when(properties.durunubiBaseUrl()).thenReturn(BASE_URL);
            when(publicDataClient.get(eq(BASE_URL), eq("/courseList"), any())).thenThrow(quotaExceeded);
            doThrow(quotaExceeded).when(publicDataClient).rethrowIfQuotaExceeded(quotaExceeded);

            assertThatThrownBy(() -> durunubiClient.fetchCourses(1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOUR_API_QUOTA_EXCEEDED);
        }
    }
}
