package live.lbtrip.domain.tourism.client.dto;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.global.util.JsonNodes;

public record EnglishPlaceItem(
    String contentId,
    String title,
    Double longitude,
    Double latitude
) {

    public static EnglishPlaceItem from(JsonNode item) {
        return new EnglishPlaceItem(
            item.path("contentid").asText(),
            item.path("title").asText(),
            JsonNodes.doubleOrNull(item, "mapx"),
            JsonNodes.doubleOrNull(item, "mapy")
        );
    }
}
