package live.lbtrip.domain.tourism.client.dto;

import java.util.List;

public record AreaBasedSample(
    int totalCount,
    List<AreaBasedItem> items
) {
}
