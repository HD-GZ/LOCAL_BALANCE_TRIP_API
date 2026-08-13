package live.lbtrip.domain.tourism.client.dto;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.global.util.JsonNodes;

public record OdiiThemeItem(
    String tid,
    String tlid,
    String title,
    Double longitude,
    Double latitude
) {

    public static OdiiThemeItem from(JsonNode item) {
        return new OdiiThemeItem(
            item.path("tid").asText(),
            item.path("tlid").asText(),
            item.path("title").asText(),
            JsonNodes.doubleOrNull(item, "mapX"),
            JsonNodes.doubleOrNull(item, "mapY")
        );
    }
}
