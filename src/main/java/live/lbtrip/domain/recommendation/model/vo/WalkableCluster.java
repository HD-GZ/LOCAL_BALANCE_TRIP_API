package live.lbtrip.domain.recommendation.model.vo;

import java.util.List;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public record WalkableCluster(
    String id,
    List<TourPlace> places
) {

    public static WalkableCluster of(String id, List<TourPlace> places) {
        return new WalkableCluster(id, List.copyOf(places));
    }

    public int size() {
        return places.size();
    }
}
