package live.lbtrip.domain.recommendation.service;

public final class WalkTimeCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final double WALK_METERS_PER_MINUTE = 67;

    private WalkTimeCalculator() {
    }

    public static Integer walkMinutes(Double fromLon, Double fromLat, Double toLon, Double toLat) {
        Double distance = distanceMeters(fromLon, fromLat, toLon, toLat);
        if (distance == null) {
            return null;
        }
        return Math.max(1, (int) Math.round(distance / WALK_METERS_PER_MINUTE));
    }

    public static Double distanceMeters(Double fromLon, Double fromLat, Double toLon, Double toLat) {
        if (fromLon == null || fromLat == null || toLon == null || toLat == null) {
            return null;
        }
        double dLat = Math.toRadians(toLat - fromLat);
        double dLon = Math.toRadians(toLon - fromLon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
