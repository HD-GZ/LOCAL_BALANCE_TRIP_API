package live.lbtrip.domain.tourism.client.dto;

import java.util.List;

public record AreaBasedSample(
    int totalCount,
    List<AreaBasedItem> items
) {

    public static AreaBasedSample of(int totalCount, List<AreaBasedItem> items) {
        return new AreaBasedSample(totalCount, items);
    }
}
