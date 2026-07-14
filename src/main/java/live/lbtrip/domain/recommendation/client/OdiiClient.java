package live.lbtrip.domain.recommendation.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.recommendation.client.dto.OdiiTheme;
import live.lbtrip.global.config.TourApiProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OdiiClient {

    private static final int MAX_RADIUS_METERS = 20000;

    private final RestClient restClient;
    private final String serviceKey;
    private final String mobileOs;
    private final String mobileApp;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OdiiClient(TourApiProperties properties) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(properties.odiiBaseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        this.restClient = RestClient.builder().uriBuilderFactory(uriFactory).build();
        this.serviceKey = properties.serviceKey();
        this.mobileOs = properties.mobileOs();
        this.mobileApp = properties.mobileApp();
    }

    public List<OdiiTheme> fetchThemesNear(double longitude, double latitude) {
        try {
            JsonNode body = get("/themeLocationBasedList", uri -> uri
                .queryParam("mapX", longitude)
                .queryParam("mapY", latitude)
                .queryParam("radius", MAX_RADIUS_METERS));

            List<OdiiTheme> themes = new ArrayList<>();
            for (JsonNode item : items(body)) {
                themes.add(new OdiiTheme(
                    item.path("tid").asText(),
                    item.path("tlid").asText(),
                    item.path("title").asText(),
                    parseCoordinate(item, "mapX"),
                    parseCoordinate(item, "mapY")
                ));
            }
            return themes;
        } catch (Exception e) {
            log.warn("Odii 테마 조회 실패 — 오디오 없이 진행", e);
            return List.of();
        }
    }

    public String fetchFirstAudioUrl(String tid, String tlid) {
        try {
            JsonNode body = get("/storyBasedList", uri -> uri
                .queryParam("tid", tid)
                .queryParam("tlid", tlid));

            for (JsonNode item : items(body)) {
                String audioUrl = item.path("audioUrl").asText(null);
                if (audioUrl != null && !audioUrl.isBlank()) {
                    return audioUrl;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Odii 스토리 조회 실패 — 오디오 없이 진행: tid={}", tid, e);
            return null;
        }
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

    private JsonNode get(String path, UnaryOperator<UriBuilder> customizer) throws Exception {
        String raw = restClient.get()
            .uri(uriBuilder -> customizer.apply(uriBuilder
                    .path(path)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", mobileOs)
                    .queryParam("MobileApp", mobileApp)
                    .queryParam("_type", "json")
                    .queryParam("langCode", "ko")
                    .queryParam("numOfRows", 100)
                    .queryParam("pageNo", 1))
                .build())
            .retrieve()
            .body(String.class);

        JsonNode root = objectMapper.readTree(raw);
        return root.path("response").path("body");
    }

    private JsonNode items(JsonNode body) {
        JsonNode item = body.path("items").path("item");
        if (item.isArray()) {
            return item;
        }
        return objectMapper.createArrayNode();
    }
}
