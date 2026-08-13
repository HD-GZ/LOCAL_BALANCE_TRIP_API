package live.lbtrip.domain.tourism.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.tourism.client.dto.VisitorStatItem;
import live.lbtrip.global.config.TourApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLabClient {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int PAGE_SIZE = 1000;

    private final PublicDataClient publicDataClient;
    private final TourApiProperties properties;

    public List<VisitorStatItem> fetchDailyVisitors(LocalDate baseDate) {
        try {
            String ymd = baseDate.format(YMD);
            JsonNode body = publicDataClient.get(
                properties.dataLabBaseUrl(), "/locgoRegnVisitrDDList", uri -> uri
                    .queryParam("numOfRows", PAGE_SIZE)
                    .queryParam("startYmd", ymd)
                    .queryParam("endYmd", ymd));

            List<VisitorStatItem> items = new ArrayList<>();
            for (JsonNode item : publicDataClient.items(body)) {
                VisitorStatItem.from(item, baseDate).ifPresent(items::add);
            }
            return items;
        } catch (Exception e) {
            publicDataClient.rethrowIfQuotaExceeded(e);
            log.warn("DataLab 방문자수 조회 실패 - 해당 일자 건너뜀: baseDate={}", baseDate, e);
            return List.of();
        }
    }
}
