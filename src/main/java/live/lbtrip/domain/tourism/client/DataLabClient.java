package live.lbtrip.domain.tourism.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.tourism.client.dto.VisitorStatItem;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.global.config.TourApiProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataLabClient {

    private static final String RESULT_OK = "0000";
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int PAGE_SIZE = 1000;

    private final RestClient restClient;
    private final String serviceKey;
    private final String mobileOs;
    private final String mobileApp;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataLabClient(TourApiProperties properties) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(properties.dataLabBaseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        ReactorClientHttpRequestFactory requestFactory = new ReactorClientHttpRequestFactory();
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = RestClient.builder()
            .uriBuilderFactory(uriFactory)
            .requestFactory(requestFactory)
            .build();
        this.serviceKey = properties.serviceKey();
        this.mobileOs = properties.mobileOs();
        this.mobileApp = properties.mobileApp();
    }

    public List<VisitorStatItem> fetchDailyVisitors(LocalDate baseDate) {
        try {
            String ymd = baseDate.format(YMD);
            String raw = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/locgoRegnVisitrDDList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", mobileOs)
                    .queryParam("MobileApp", mobileApp)
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", PAGE_SIZE)
                    .queryParam("pageNo", 1)
                    .queryParam("startYmd", ymd)
                    .queryParam("endYmd", ymd)
                    .build())
                .retrieve()
                .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!RESULT_OK.equals(resultCode)) {
                log.warn("DataLab 오류 응답 - 해당 일자 건너뜀: baseDate={}, resultCode={}", baseDate, resultCode);
                return List.of();
            }

            List<VisitorStatItem> items = new ArrayList<>();
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");
            if (!itemNode.isArray()) {
                return List.of();
            }
            for (JsonNode item : itemNode) {
                VisitorType.fromCode(item.path("touDivCd").asText())
                    .ifPresent(type -> items.add(new VisitorStatItem(
                        item.path("signguCode").asText(),
                        type,
                        item.path("touNum").asDouble(0),
                        baseDate)));
            }
            return items;
        } catch (Exception e) {
            log.warn("DataLab 방문자수 조회 실패 - 해당 일자 건너뜀: baseDate={}", baseDate, e);
            return List.of();
        }
    }
}
