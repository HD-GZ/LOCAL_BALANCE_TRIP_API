package live.lbtrip.domain.tourism.client.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.global.util.JsonNodes;

public record TourEventItem(
    String contentId,
    String title,
    LocalDate eventStart,
    LocalDate eventEnd,
    String imageUrl,
    Double longitude,
    Double latitude,
    String address,
    String tel
) {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static TourEventItem from(JsonNode item) {
        LocalDate start = dateOrNull(item, "eventstartdate");
        LocalDate end = dateOrNull(item, "eventenddate");
        if (start == null || end == null) {
            return null;
        }
        return new TourEventItem(
            item.path("contentid").asText(),
            item.path("title").asText(),
            start,
            end,
            JsonNodes.textOrNull(item, "firstimage"),
            JsonNodes.doubleOrNull(item, "mapx"),
            JsonNodes.doubleOrNull(item, "mapy"),
            JsonNodes.textOrNull(item, "addr1"),
            JsonNodes.textOrNull(item, "tel")
        );
    }

    private static LocalDate dateOrNull(JsonNode item, String field) {
        String value = JsonNodes.textOrNull(item, field);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
