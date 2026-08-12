package live.lbtrip.global.util;

/**
 * 하버사인 공식 기반 좌표 거리 계산 유틸.
 * 지구를 반경 6,371km의 구로 근사해 두 위경도 사이의 대원(great-circle)
 * 직선거리를 미터로 반환한다. 좌표가 하나라도 없으면 계산 불가로 보고 null을 반환한다.
 */
public final class GeoDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private GeoDistanceCalculator() {
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
