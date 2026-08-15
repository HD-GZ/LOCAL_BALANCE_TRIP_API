package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.vo.WalkableCluster;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.config.RecommendationProperties;
import live.lbtrip.global.util.GeoDistanceCalculator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalkableClusterBuilder {

    static final int MIN_PLACES_PER_CLUSTER = 3;

    private final RecommendationProperties recommendationProperties;

    public List<WalkableCluster> build(List<TourPlace> places) {
        List<TourPlace> located = new ArrayList<>();
        for (TourPlace place : places) {
            if (place.getLatitude() != null && place.getLongitude() != null) {
                located.add(place);
            }
        }
        located.sort(Comparator.comparing(TourPlace::getContentId));

        List<WalkableCluster> clusters = List.of();
        for (int radiusMeters : recommendationProperties.walkClusterRadiiMeters()) {
            clusters = buildWithRadius(located, radiusMeters);
            if (capacity(clusters) >= recommendationProperties.maxCourses()) {
                break;
            }
        }
        return clusters;
    }

    private List<WalkableCluster> buildWithRadius(List<TourPlace> located, int radiusMeters) {
        List<TourPlace> remaining = new ArrayList<>(located);
        List<WalkableCluster> clusters = new ArrayList<>();
        while (!remaining.isEmpty()) {
            List<TourPlace> best = List.of();
            for (TourPlace center : remaining) {
                List<TourPlace> neighbors = neighborsWithin(center, remaining, radiusMeters);
                if (neighbors.size() > best.size()) {
                    best = neighbors;
                }
            }
            if (best.size() < MIN_PLACES_PER_CLUSTER) {
                break;
            }
            clusters.add(WalkableCluster.of(String.valueOf(clusters.size() + 1), best));
            remaining.removeAll(best);
        }
        return List.copyOf(clusters);
    }

    private List<TourPlace> neighborsWithin(TourPlace center, List<TourPlace> candidates, int radiusMeters) {
        List<TourPlace> neighbors = new ArrayList<>();
        for (TourPlace candidate : candidates) {
            Double distance = GeoDistanceCalculator.distanceMeters(
                center.getLongitude(), center.getLatitude(),
                candidate.getLongitude(), candidate.getLatitude());
            if (distance != null && distance <= radiusMeters) {
                neighbors.add(candidate);
            }
        }
        return neighbors;
    }

    private int capacity(List<WalkableCluster> clusters) {
        int capacity = 0;
        for (WalkableCluster cluster : clusters) {
            capacity += cluster.size() / MIN_PLACES_PER_CLUSTER;
        }
        return capacity;
    }
}
