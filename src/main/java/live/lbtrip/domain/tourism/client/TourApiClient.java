package live.lbtrip.domain.tourism.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.dto.AreaBasedItem;
import live.lbtrip.domain.tourism.client.dto.AreaBasedSample;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.global.config.TourApiProperties;
import live.lbtrip.global.util.JsonNodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private static final int STATS_SAMPLE_SIZE = 1000;
    private static final int PLACES_PAGE_SIZE = 15;

    private final PublicDataClient publicDataClient;
    private final TourApiProperties properties;

    public AreaBasedSample fetchAreaBasedSample(RegionCandidate candidate) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", STATS_SAMPLE_SIZE)
            .queryParam("arrange", "C")
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        List<AreaBasedItem> items = new ArrayList<>();
        for (JsonNode item : publicDataClient.items(body)) {
            items.add(AreaBasedItem.from(item));
        }
        return AreaBasedSample.of(body.path("totalCount").asInt(0), items);
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
            places.add(TourPlaceItem.from(item));
        }
        return places;
    }

    public String fetchOverview(String contentId) {
        JsonNode body = get("/detailCommon2", uri -> uri.queryParam("contentId", contentId));
        for (JsonNode item : publicDataClient.items(body)) {
            String overview = JsonNodes.textOrNull(item, "overview");
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
