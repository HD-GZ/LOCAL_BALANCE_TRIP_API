package live.lbtrip.domain.tourism.model.vo;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record Centroid(
    double longitude,
    double latitude
) {

    public static <T> Optional<Centroid> of(
        List<T> points,
        Function<T, Double> longitudeGetter,
        Function<T, Double> latitudeGetter
    ) {
        double longitudeSum = 0;
        double latitudeSum = 0;
        int count = 0;
        for (T point : points) {
            Double longitude = longitudeGetter.apply(point);
            Double latitude = latitudeGetter.apply(point);
            if (longitude == null || latitude == null) {
                continue;
            }
            longitudeSum += longitude;
            latitudeSum += latitude;
            count++;
        }
        if (count == 0) {
            return Optional.empty();
        }
        return Optional.of(new Centroid(longitudeSum / count, latitudeSum / count));
    }
}
