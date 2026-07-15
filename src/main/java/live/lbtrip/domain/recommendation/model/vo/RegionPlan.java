package live.lbtrip.domain.recommendation.model.vo;

import java.util.List;

public record RegionPlan(
    String regionName,
    String ldongRegnCd,
    String ldongSignguCd,
    String imageUrl,
    String reason,
    List<CoursePlanData> courses
) {

    public static RegionPlan of(
        String regionName, String ldongRegnCd, String ldongSignguCd,
        String imageUrl, String reason, List<CoursePlanData> courses
    ) {
        return new RegionPlan(regionName, ldongRegnCd, ldongSignguCd, imageUrl, reason, courses);
    }

    public record CoursePlanData(
        String name,
        String reason,
        String imageUrl,
        List<PlaceSnapshot> places
    ) {

        public static CoursePlanData of(String name, String reason, String imageUrl, List<PlaceSnapshot> places) {
            return new CoursePlanData(name, reason, imageUrl, places);
        }
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

        public static PlaceSnapshot of(
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
            return new PlaceSnapshot(
                visitOrder, name, overview, imageUrl, latitude, longitude, walkMinutes, hasAudio, audioUrl);
        }
    }
}
