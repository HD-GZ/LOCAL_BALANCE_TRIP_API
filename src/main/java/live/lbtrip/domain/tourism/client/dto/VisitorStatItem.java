package live.lbtrip.domain.tourism.client.dto;

import java.time.LocalDate;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import live.lbtrip.domain.tourism.model.enums.VisitorType;

public record VisitorStatItem(
    String signguCode,
    VisitorType visitorType,
    double visitorCount,
    LocalDate baseDate
) {

    public static Optional<VisitorStatItem> from(JsonNode item, LocalDate baseDate) {
        return VisitorType.fromCode(item.path("touDivCd").asText())
            .map(visitorType -> new VisitorStatItem(
                item.path("signguCode").asText(),
                visitorType,
                item.path("touNum").asDouble(0),
                baseDate
            ));
    }
}
