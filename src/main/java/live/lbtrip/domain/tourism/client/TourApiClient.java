package live.lbtrip.domain.tourism.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.dto.AreaBasedItem;
import live.lbtrip.domain.tourism.client.dto.AreaBasedSample;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
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

    public List<TourPlaceItem> fetchPlaces(
        Locale locale, String ldongRegnCd, String ldongSignguCd, TourContentType contentType
    ) {
        JsonNode body = get(locale, "/areaBasedList2", uri -> uri
            .queryParam("numOfRows", PLACES_PAGE_SIZE)
            .queryParam("arrange", "O")
            .queryParam("contentTypeId", contentType.codeFor(locale))
            .queryParam("lDongRegnCd", ldongRegnCd)
            .queryParam("lDongSignguCd", ldongSignguCd));

        List<TourPlaceItem> places = new ArrayList<>();
        for (JsonNode item : publicDataClient.items(body)) {
            places.add(TourPlaceItem.from(item, contentType));
        }
        return places;
    }

    public String fetchOverview(Locale locale, String contentId) {
        JsonNode body = get(locale, "/detailCommon2", uri -> uri.queryParam("contentId", contentId));
        return firstOverview(body);
    }

    private String firstOverview(JsonNode body) {
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

    private JsonNode get(Locale locale, String path, UnaryOperator<UriBuilder> customizer) {
        return publicDataClient.get(baseUrlFor(locale), path, customizer);
    }

    private String baseUrlFor(Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? properties.engBaseUrl() : properties.baseUrl();
    }
}
