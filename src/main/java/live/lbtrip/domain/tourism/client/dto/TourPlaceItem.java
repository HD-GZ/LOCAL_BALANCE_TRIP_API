package live.lbtrip.domain.tourism.client.dto;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.global.util.JsonNodes;

public record TourPlaceItem(
    String contentId,
    String title,
    int contentTypeId,
    String imageUrl,
    Double longitude,
    Double latitude
) {

    public static TourPlaceItem from(JsonNode item, TourContentType contentType) {
        return new TourPlaceItem(
            item.path("contentid").asText(),
            item.path("title").asText(),
            contentType.getCode(),
            JsonNodes.textOrNull(item, "firstimage"),
            JsonNodes.doubleOrNull(item, "mapx"),
            JsonNodes.doubleOrNull(item, "mapy")
        );
    }
}
