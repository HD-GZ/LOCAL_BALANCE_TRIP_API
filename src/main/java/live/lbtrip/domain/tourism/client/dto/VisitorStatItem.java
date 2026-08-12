package live.lbtrip.domain.tourism.client.dto;

import java.time.LocalDate;

import live.lbtrip.domain.tourism.model.enums.VisitorType;

public record VisitorStatItem(
    String signguCode,
    VisitorType visitorType,
    double visitorCount,
    LocalDate baseDate
) {
}
