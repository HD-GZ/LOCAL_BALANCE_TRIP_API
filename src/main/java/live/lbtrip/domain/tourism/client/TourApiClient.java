package live.lbtrip.domain.tourism.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.vo.CategoryGroupMapping;
import live.lbtrip.domain.tourism.service.CategoryGroupClassifier;
import live.lbtrip.global.config.TourApiProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private static final int STATS_SAMPLE_SIZE = 1000;
    private static final int PLACES_PAGE_SIZE = 15;

    private final PublicDataClient publicDataClient;
    private final TourApiProperties properties;
    private final CategoryGroupClassifier categoryGroupClassifier;

    public RegionStats fetchRegionStats(RegionCandidate candidate) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", STATS_SAMPLE_SIZE)
            .queryParam("arrange", "C")
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        CategoryGroupMapping mapping = categoryGroupClassifier.load();
        int totalCount = body.path("totalCount").asInt(0);
        int sampleSize = 0;
        Map<Integer, Integer> typeCounts = new HashMap<>();
        Map<CategoryGroup, Integer> groupCounts = new EnumMap<>(CategoryGroup.class);
        for (JsonNode item : publicDataClient.items(body)) {
            typeCounts.merge(item.path("contenttypeid").asInt(0), 1, Integer::sum);
            for (CategoryGroup group : mapping.classify(
                item.path("cat1").asText(null),
                item.path("cat2").asText(null),
                item.path("cat3").asText(null))) {
                groupCounts.merge(group, 1, Integer::sum);
            }
            sampleSize++;
        }
        return new RegionStats(
            candidate.getId(), candidate.getName(),
            totalCount, sampleSize, typeCounts, groupCounts);
    }

    public List<TourPlaceItem> fetchPlaces(String ldongRegnCd, String ldongSignguCd, int contentTypeId) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", PLACES_PAGE_SIZE)
            .queryParam("arrange", "O")
            .queryParam("contentTypeId", contentTypeId)
            .queryParam("lDongRegnCd", ldongRegnCd)
            .queryParam("lDongSignguCd", ldongSignguCd));

        List<TourPlaceItem> places = new ArrayList<>();
        for (JsonNode item : publicDataClient.items(body)) {
            places.add(new TourPlaceItem(
                item.path("contentid").asText(),
                item.path("title").asText(),
                item.path("contenttypeid").asInt(0),
                publicDataClient.textOf(item, "firstimage"),
                publicDataClient.coordinateOf(item, "mapx"),
                publicDataClient.coordinateOf(item, "mapy")
            ));
        }
        return places;
    }

    public String fetchOverview(String contentId) {
        JsonNode body = get("/detailCommon2", uri -> uri.queryParam("contentId", contentId));
        for (JsonNode item : publicDataClient.items(body)) {
            String overview = publicDataClient.textOf(item, "overview");
            if (overview != null) {
                return overview;
            }
        }
        return null;
    }

    private JsonNode get(String path, UnaryOperator<UriBuilder> customizer) {
        return publicDataClient.get(properties.baseUrl(), path, customizer);
    }
}
