package live.lbtrip.domain.tourism.client.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record AreaBasedItem(
    int contentTypeId,
    String cat1,
    String cat2,
    String cat3
) {

    public static AreaBasedItem from(JsonNode item) {
        return new AreaBasedItem(
            item.path("contenttypeid").asInt(0),
            item.path("cat1").asText(null),
            item.path("cat2").asText(null),
            item.path("cat3").asText(null)
        );
    }
}
