package live.lbtrip.domain.tourism.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.tourism.client.dto.OdiiThemeItem;
import live.lbtrip.global.config.TourApiProperties;
import live.lbtrip.global.util.JsonNodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OdiiClient {

    private static final int MAX_RADIUS_METERS = 20000;
    private static final int PAGE_SIZE = 100;

    private final PublicDataClient publicDataClient;
    private final TourApiProperties properties;

    public List<OdiiThemeItem> fetchThemesNear(double longitude, double latitude) {
        try {
            JsonNode body = get("/themeLocationBasedList", uri -> uri
                .queryParam("mapX", longitude)
                .queryParam("mapY", latitude)
                .queryParam("radius", MAX_RADIUS_METERS));

            List<OdiiThemeItem> themes = new ArrayList<>();
            for (JsonNode item : publicDataClient.items(body)) {
                themes.add(OdiiThemeItem.from(item));
            }
            return themes;
        } catch (Exception e) {
            publicDataClient.rethrowIfQuotaExceeded(e);
            log.warn("Odii 테마 조회 실패 — 오디오 없이 진행", e);
            return List.of();
        }
    }

    public String fetchFirstAudioUrl(String tid, String tlid) {
        try {
            JsonNode body = get("/storyBasedList", uri -> uri
                .queryParam("tid", tid)
                .queryParam("tlid", tlid));

            for (JsonNode item : publicDataClient.items(body)) {
                String audioUrl = JsonNodes.textOrNull(item, "audioUrl");
                if (audioUrl != null) {
                    return audioUrl;
                }
            }
            return null;
        } catch (Exception e) {
            publicDataClient.rethrowIfQuotaExceeded(e);
            log.warn("Odii 스토리 조회 실패 — 오디오 없이 진행: tid={}", tid, e);
            return null;
        }
    }

    private JsonNode get(String path, UnaryOperator<UriBuilder> customizer) {
        return publicDataClient.get(properties.odiiBaseUrl(), path, uri -> customizer.apply(uri
            .queryParam("langCode", "ko")
            .queryParam("numOfRows", PAGE_SIZE)));
    }
}
