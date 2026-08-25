package live.lbtrip.domain.tourism.client.dto;

import java.math.BigDecimal;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.global.util.JsonNodes;

public record DurunubiCourseItem(
    String crsIdx,
    String crsKorNm,
    BigDecimal crsDstnc,
    Integer crsTotlRqrdHour,
    Integer crsLevel,
    String sigun,
    String gpxpath,
    String brdDiv,
    String routeIdx,
    String crsSummary
) {

    public static Optional<DurunubiCourseItem> from(JsonNode item) {
        String crsIdx = JsonNodes.textOrNull(item, "crsIdx");
        if (crsIdx == null) {
            return Optional.empty();
        }
        return Optional.of(new DurunubiCourseItem(
            crsIdx,
            JsonNodes.textOrNull(item, "crsKorNm"),
            decimalOrNull(item, "crsDstnc"),
            integerOrNull(item, "crsTotlRqrdHour"),
            integerOrNull(item, "crsLevel"),
            JsonNodes.textOrNull(item, "sigun"),
            JsonNodes.textOrNull(item, "gpxpath"),
            JsonNodes.textOrNull(item, "brdDiv"),
            JsonNodes.textOrNull(item, "routeIdx"),
            JsonNodes.textOrNull(item, "crsSummary")
        ));
    }

    private static BigDecimal decimalOrNull(JsonNode item, String field) {
        Double value = JsonNodes.doubleOrNull(item, field);
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static Integer integerOrNull(JsonNode item, String field) {
        Double value = JsonNodes.doubleOrNull(item, field);
        return value == null ? null : value.intValue();
    }
}
