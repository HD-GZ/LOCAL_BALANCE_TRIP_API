package live.lbtrip.domain.tourism.client.dto;

import java.util.List;

public record TourPlacePage(
    int totalCount,
    List<TourPlaceItem> items
) {

    public static TourPlacePage of(int totalCount, List<TourPlaceItem> items) {
        return new TourPlacePage(totalCount, items);
    }

    public boolean truncated() {
        return totalCount > items.size();
    }
}
