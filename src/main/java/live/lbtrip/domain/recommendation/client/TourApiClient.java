package live.lbtrip.domain.recommendation.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.recommendation.client.dto.RegionStats;
import live.lbtrip.domain.recommendation.client.dto.TourPlace;
import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;
import live.lbtrip.global.config.TourApiProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TourApiClient {

    private static final String RESULT_OK = "0000";
    private static final int STATS_SAMPLE_SIZE = 100;
    private static final int PLACES_PAGE_SIZE = 15;

    private final RestClient restClient;
    private final String serviceKey;
    private final String mobileOs;
    private final String mobileApp;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TourApiClient(TourApiProperties properties) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(properties.baseUrl());
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

    public RegionStats fetchRegionStats(RegionCandidate candidate) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", STATS_SAMPLE_SIZE)
            .queryParam("arrange", "C")
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        int totalCount = body.path("totalCount").asInt(0);
        int sampleSize = 0;
        Map<Integer, Integer> typeCounts = new HashMap<>();
        for (JsonNode item : items(body)) {
            typeCounts.merge(item.path("contenttypeid").asInt(0), 1, Integer::sum);
            sampleSize++;
        }
        return new RegionStats(
            candidate.getName(), candidate.getLdongRegnCd(), candidate.getLdongSignguCd(),
            totalCount, sampleSize, typeCounts);
    }

    public List<TourPlace> fetchPlaces(String ldongRegnCd, String ldongSignguCd, int contentTypeId) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", PLACES_PAGE_SIZE)
            .queryParam("arrange", "O")
            .queryParam("contentTypeId", contentTypeId)
            .queryParam("lDongRegnCd", ldongRegnCd)
            .queryParam("lDongSignguCd", ldongSignguCd));

        List<TourPlace> places = new ArrayList<>();
        for (JsonNode item : items(body)) {
            places.add(new TourPlace(
                item.path("contentid").asText(),
                item.path("title").asText(),
                item.path("contenttypeid").asInt(0),
                parseText(item, "firstimage"),
                parseCoordinate(item, "mapx"),
                parseCoordinate(item, "mapy")
            ));
        }
        return places;
    }

    public String fetchOverview(String contentId) {
        JsonNode body = get("/detailCommon2", uri -> uri.queryParam("contentId", contentId));
        for (JsonNode item : items(body)) {
            String overview = item.path("overview").asText(null);
            if (overview != null && !overview.isBlank()) {
                return overview;
            }
        }
        return null;
    }

    private Double parseCoordinate(JsonNode item, String field) {
        String value = item.path(field).asText("");
        if (value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseText(JsonNode item, String field) {
        String value = item.path(field).asText("");
        return value.isBlank() ? null : value;
    }

    private JsonNode get(String path, UnaryOperator<UriBuilder> customizer) {
        try {
            String raw = restClient.get()
                .uri(uriBuilder -> customizer.apply(uriBuilder
                        .path(path)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", mobileOs)
                        .queryParam("MobileApp", mobileApp)
                        .queryParam("_type", "json")
                        .queryParam("pageNo", 1))
                    .build())
                .retrieve()
                .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!RESULT_OK.equals(resultCode)) {
                log.error("TourAPI 오류 응답: path={}, resultCode={}", path, resultCode);
                throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
            }
            return root.path("response").path("body");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("TourAPI 호출 실패: path={}", path, e);
            throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
        }
    }

    private JsonNode items(JsonNode body) {
        JsonNode item = body.path("items").path("item");
        if (item.isArray()) {
            return item;
        }
        return objectMapper.createArrayNode();
    }
}
