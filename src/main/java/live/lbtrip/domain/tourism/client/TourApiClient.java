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
import live.lbtrip.domain.tourism.client.dto.TourPlacePage;
import live.lbtrip.global.config.TourApiProperties;
import live.lbtrip.global.util.JsonNodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private static final int PAGE_SIZE = 1000;

    private final PublicDataClient publicDataClient;
    private final TourApiProperties properties;

    public AreaBasedSample fetchAreaBasedSample(RegionCandidate candidate) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", PAGE_SIZE)
            .queryParam("arrange", "C")
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        List<AreaBasedItem> items = new ArrayList<>();
        for (JsonNode item : publicDataClient.items(body)) {
            items.add(AreaBasedItem.from(item));
        }
        return AreaBasedSample.of(body.path("totalCount").asInt(0), items);
    }

    public TourPlacePage fetchPlaces(RegionCandidate candidate) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", PAGE_SIZE)
            .queryParam("arrange", "O")
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        List<TourPlaceItem> places = new ArrayList<>();
        for (JsonNode item : publicDataClient.items(body)) {
            places.add(TourPlaceItem.from(item));
        }
        return TourPlacePage.of(body.path("totalCount").asInt(0), places);
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
