package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.vo.CourseCandidateGroup;
import live.lbtrip.domain.tourism.model.entity.TourPlace;

@Component
public class CourseCandidateClusterer {

    private static final int MIN_PLACES_PER_GROUP = 3;
    private static final int MAX_GROUPS = 3;
    private static final int MAX_CANDIDATES_PER_GROUP = 15;

    public List<CourseCandidateGroup> cluster(List<TourPlace> places) {
        List<TourPlace> valid = places.stream().filter(this::hasValidCoordinates).toList();
        if (valid.size() < MIN_PLACES_PER_GROUP) {
            return List.of();
        }

        int groupCount = Math.min(MAX_GROUPS, valid.size() / MIN_PLACES_PER_GROUP);
        Map<TourPlace, Integer> inputOrder = inputOrder(valid);
        List<TourPlace> centers = selectCenters(valid, inputOrder, groupCount);

        List<CourseCandidateGroup> groups = new ArrayList<>();
        for (int i = 0; i < centers.size(); i++) {
            TourPlace center = centers.get(i);
            List<TourPlace> candidates = valid.stream()
                .sorted(Comparator
                    .comparingDouble((TourPlace place) -> distance(center, place))
                    .thenComparingInt(inputOrder::get)
                    .thenComparing(TourPlace::getContentId))
                .limit(MAX_CANDIDATES_PER_GROUP)
                .toList();
            groups.add(CourseCandidateGroup.of("G" + (i + 1), candidates));
        }
        return List.copyOf(groups);
    }

    private List<TourPlace> selectCenters(
        List<TourPlace> places,
        Map<TourPlace, Integer> inputOrder,
        int groupCount
    ) {
        Comparator<TourPlace> tieBreaker = Comparator
            .comparingInt((TourPlace place) -> inputOrder.get(place))
            .thenComparing(TourPlace::getContentId);
        List<TourPlace> centers = new ArrayList<>();
        TourPlace first = places.stream()
            .min(Comparator
                .comparingDouble((TourPlace candidate) -> totalDistance(candidate, places))
                .thenComparing(tieBreaker))
            .orElseThrow();
        centers.add(first);

        while (centers.size() < groupCount) {
            TourPlace next = places.stream()
                .filter(place -> !centers.contains(place))
                .max(Comparator
                    .comparingDouble((TourPlace candidate) -> minimumDistance(candidate, centers))
                    .thenComparing(tieBreaker.reversed()))
                .orElseThrow();
            centers.add(next);
        }
        return centers;
    }

    private Map<TourPlace, Integer> inputOrder(List<TourPlace> places) {
        Map<TourPlace, Integer> order = new HashMap<>();
        for (int i = 0; i < places.size(); i++) {
            order.put(places.get(i), i);
        }
        return order;
    }

    private double totalDistance(TourPlace candidate, List<TourPlace> places) {
        return places.stream().mapToDouble(place -> distance(candidate, place)).sum();
    }

    private double minimumDistance(TourPlace candidate, List<TourPlace> centers) {
        return centers.stream().mapToDouble(center -> distance(candidate, center)).min().orElse(0);
    }

    private double distance(TourPlace from, TourPlace to) {
        return WalkTimeCalculator.distanceMeters(
            from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude());
    }

    private boolean hasValidCoordinates(TourPlace place) {
        Double longitude = place.getLongitude();
        Double latitude = place.getLatitude();
        return longitude != null && latitude != null
            && Double.isFinite(longitude) && Double.isFinite(latitude)
            && longitude >= -180 && longitude <= 180
            && latitude >= -90 && latitude <= 90;
    }
}
