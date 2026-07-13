package live.lbtrip.domain.recommendation.service.dto;

import java.util.List;

public record RegionPlan(
    String regionName,
    String imageUrl,
    String reason,
    List<CoursePlanData> courses
) {

    public record CoursePlanData(
        String name,
        String reason,
        String imageUrl,
        List<PlaceSnapshot> places
    ) {
    }

    public record PlaceSnapshot(
        int visitOrder,
        String name,
        String overview,
        String imageUrl,
        Double latitude,
        Double longitude,
        Integer walkMinutes,
        boolean hasAudio,
        String audioUrl
    ) {
    }
}
