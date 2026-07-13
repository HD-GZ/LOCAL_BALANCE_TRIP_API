package live.lbtrip.domain.recommendation.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TourApiClient(TourApiProperties properties) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(properties.baseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        this.restClient = RestClient.builder().uriBuilderFactory(uriFactory).build();
        this.serviceKey = properties.serviceKey();
    }

    public RegionStats fetchRegionStats(RegionCandidate candidate) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", STATS_SAMPLE_SIZE)
            .queryParam("arrange", "C")
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        int totalCount = body.path("totalCount").asInt(0);
        Map<Integer, Integer> typeCounts = new HashMap<>();
        for (JsonNode item : items(body)) {
            typeCounts.merge(item.path("contenttypeid").asInt(0), 1, Integer::sum);
        }
        return new RegionStats(candidate, totalCount, typeCounts);
    }

    public List<TourPlace> fetchPlaces(RegionCandidate candidate, int contentTypeId) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", PLACES_PAGE_SIZE)
            .queryParam("arrange", "O")
            .queryParam("contentTypeId", contentTypeId)
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        List<TourPlace> places = new ArrayList<>();
        for (JsonNode item : items(body)) {
            places.add(new TourPlace(
                item.path("contentid").asText(),
                item.path("title").asText(),
                item.path("contenttypeid").asInt(0),
                item.path("firstimage").asText(null),
                item.path("mapx").isMissingNode() ? null : item.path("mapx").asDouble(),
                item.path("mapy").isMissingNode() ? null : item.path("mapy").asDouble()
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

    private JsonNode get(String path, UnaryOperator<UriBuilder> customizer) {
        try {
            String raw = restClient.get()
                .uri(uriBuilder -> customizer.apply(uriBuilder
                        .path(path)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "lbtrip")
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
