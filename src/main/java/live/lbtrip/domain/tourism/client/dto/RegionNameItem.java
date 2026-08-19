package live.lbtrip.domain.tourism.client.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record RegionNameItem(
    String ldongRegnCd,
    String ldongSignguCd,
    String regionName,
    String signguName
) {

    public static RegionNameItem from(JsonNode item) {
        return new RegionNameItem(
            item.path("lDongRegnCd").asText(),
            item.path("lDongSignguCd").asText(),
            item.path("lDongRegnNm").asText(),
            item.path("lDongSignguNm").asText()
        );
    }

    public String fullName() {
        return "%s, %s".formatted(signguName, regionName);
    }
}
