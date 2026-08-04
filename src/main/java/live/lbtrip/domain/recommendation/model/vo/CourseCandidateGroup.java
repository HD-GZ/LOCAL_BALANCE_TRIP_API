package live.lbtrip.domain.recommendation.model.vo;

import java.util.List;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public record CourseCandidateGroup(
    String id,
    List<TourPlace> candidates
) {

    public static CourseCandidateGroup of(String id, List<TourPlace> candidates) {
        return new CourseCandidateGroup(id, List.copyOf(candidates));
    }
}
