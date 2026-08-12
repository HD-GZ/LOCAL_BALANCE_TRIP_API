package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.vo.RoutedPlace;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

/**
 * 코스 장소들의 방문 순서를 확정하고 구간별 도보 시간을 붙여 동선을 계획한다.
 *
 * <p>1) 방문 순서: 장소가 최대 5곳이므로 순열 전수조사(5! = 120가지)로 모든 순서의
 * 총 이동거리(연속 구간 거리 합)를 비교해 최소 경로를 채택한다. 근사 없이 항상 최적이다.
 * <ul>
 *   <li>거리 동률(부동소수점 오차 허용치 1e-6m 이내)은 contentId 사전순으로 앞서는
 *       경로를 채택한다 — 같은 입력이면 항상 같은 결과가 나오게 하기 위함</li>
 *   <li>좌표 없는 장소가 낀 경로는 거리를 무한대로 취급해 후순위로 밀어낸다.
 *       모든 경로가 계산 불가면 입력 순서를 그대로 사용한다</li>
 * </ul>
 *
 * <p>2) 도보 시간: 확정된 순서의 연속한 두 장소마다
 * 거리 ÷ 67m/분(약 4km/h)을 반올림해 붙인다(최소 1분). 첫 장소와
 * 좌표를 알 수 없는 구간은 null — 화면에서 도보 시간 미표시를 뜻한다.
 *
 * <p>거리는 하버사인 공식으로 구한다: 지구를 반경 6,371km의 구로 근사한
 * 대원(great-circle) 직선거리라 실제 도로 거리보다 짧게 나오지만,
 * 시군구 안 수 km 단위 안내 용도로는 충분하다.
 */
@Component
public class CourseRoutePlanner {

    private static final int MAX_PLACES = 5;
    private static final double DISTANCE_EQUALITY_EPSILON_METERS = 0.000_001;
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final double WALK_METERS_PER_MINUTE = 67;

    public List<RoutedPlace> plan(List<TourPlace> places) {
        List<TourPlace> ordered = optimizeOrder(places);

        List<RoutedPlace> routed = new ArrayList<>();
        TourPlace previous = null;
        for (TourPlace place : ordered) {
            routed.add(RoutedPlace.of(place, previous == null ? null : walkMinutes(previous, place)));
            previous = place;
        }
        return List.copyOf(routed);
    }

    private List<TourPlace> optimizeOrder(List<TourPlace> places) {
        if (places.size() > MAX_PLACES) {
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        if (places.size() < 2) {
            return List.copyOf(places);
        }

        BestRoute bestRoute = new BestRoute(places);
        permute(new ArrayList<>(places), 0, bestRoute);
        return bestRoute.places;
    }

    private void permute(List<TourPlace> places, int index, BestRoute bestRoute) {
        if (index == places.size()) {
            bestRoute.consider(places, totalDistance(places));
            return;
        }
        for (int i = index; i < places.size(); i++) {
            swap(places, index, i);
            permute(places, index + 1, bestRoute);
            swap(places, index, i);
        }
    }

    private double totalDistance(List<TourPlace> places) {
        double total = 0;
        for (int i = 1; i < places.size(); i++) {
            Double distance = distanceMeters(places.get(i - 1), places.get(i));
            if (distance == null || !Double.isFinite(distance)) {
                return Double.POSITIVE_INFINITY;
            }
            total += distance;
        }
        return total;
    }

    private void swap(List<TourPlace> places, int left, int right) {
        TourPlace value = places.get(left);
        places.set(left, places.get(right));
        places.set(right, value);
    }

    private Integer walkMinutes(TourPlace from, TourPlace to) {
        Double distance = distanceMeters(from, to);
        if (distance == null) {
            return null;
        }
        return Math.max(1, (int) Math.round(distance / WALK_METERS_PER_MINUTE));
    }

    private Double distanceMeters(TourPlace from, TourPlace to) {
        Double fromLon = from.getLongitude();
        Double fromLat = from.getLatitude();
        Double toLon = to.getLongitude();
        Double toLat = to.getLatitude();
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

    private static final class BestRoute {

        private double distance = Double.POSITIVE_INFINITY;
        private List<TourPlace> places;

        private BestRoute(List<TourPlace> fallback) {
            this.places = List.copyOf(fallback);
        }

        private void consider(List<TourPlace> candidate, double candidateDistance) {
            double difference = candidateDistance - distance;
            if (difference < -DISTANCE_EQUALITY_EPSILON_METERS
                || (Math.abs(difference) <= DISTANCE_EQUALITY_EPSILON_METERS
                    && compareContentIds(candidate, places) < 0)) {
                distance = candidateDistance;
                places = List.copyOf(candidate);
            }
        }

        private int compareContentIds(List<TourPlace> left, List<TourPlace> right) {
            for (int i = 0; i < left.size(); i++) {
                int comparison = left.get(i).getContentId().compareTo(right.get(i).getContentId());
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        }
    }
}
