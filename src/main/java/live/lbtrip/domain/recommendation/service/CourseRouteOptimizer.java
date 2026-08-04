package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@Component
public class CourseRouteOptimizer {

    private static final int MAX_PLACES = 5;
    private static final double DISTANCE_EQUALITY_EPSILON_METERS = 0.000_001;

    public List<TourPlace> optimize(List<TourPlace> places) {
        if (places.size() > MAX_PLACES) {
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        if (places.size() < 2) {
            return List.copyOf(places);
        }

        BestRoute bestRoute = new BestRoute();
        permute(new ArrayList<>(places), 0, bestRoute);
        return List.copyOf(bestRoute.places);
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
            TourPlace previous = places.get(i - 1);
            TourPlace current = places.get(i);
            Double distance = WalkTimeCalculator.distanceMeters(
                previous.getLongitude(), previous.getLatitude(),
                current.getLongitude(), current.getLatitude());
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

    private static final class BestRoute {

        private double distance = Double.POSITIVE_INFINITY;
        private List<TourPlace> places = List.of();

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
            if (right.isEmpty()) {
                return -1;
            }
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
