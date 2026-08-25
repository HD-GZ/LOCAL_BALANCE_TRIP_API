package live.lbtrip.domain.tourism.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.tourism.client.dto.DurunubiCourseItem;
import live.lbtrip.global.config.TourApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DurunubiClient {

    public static final int PAGE_SIZE = 1000;

    private final PublicDataClient publicDataClient;
    private final TourApiProperties properties;

    public List<DurunubiCourseItem> fetchCourses(int pageNo) {
        try {
            JsonNode body = publicDataClient.get(properties.durunubiBaseUrl(), "/courseList", uri -> uri
                .replaceQueryParam("pageNo", pageNo)
                .queryParam("numOfRows", PAGE_SIZE));

            List<DurunubiCourseItem> items = new ArrayList<>();
            for (JsonNode item : publicDataClient.items(body)) {
                DurunubiCourseItem.from(item).ifPresent(items::add);
            }
            return items;
        } catch (Exception e) {
            publicDataClient.rethrowIfQuotaExceeded(e);
            log.warn("두루누비 코스 조회 실패 - 해당 페이지 건너뜀: pageNo={}", pageNo, e);
            return List.of();
        }
    }
}
