package live.lbtrip.domain.recommendation.model.vo;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public record RoutedPlace(
    TourPlace place,
    Integer walkMinutes
) {

    public static RoutedPlace of(TourPlace place, Integer walkMinutes) {
        return new RoutedPlace(place, walkMinutes);
    }
}
