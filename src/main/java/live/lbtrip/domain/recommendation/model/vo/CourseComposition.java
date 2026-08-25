package live.lbtrip.domain.recommendation.model.vo;

import java.util.List;

public record CourseComposition(
    String regionReason,
    List<CoursePlan> courses
) {

    public static CourseComposition of(String regionReason, List<CoursePlan> courses) {
        return new CourseComposition(regionReason, courses);
    }

    public record CoursePlan(
        String name,
        String reason,
        List<PlacePlan> places
    ) {

        public static CoursePlan of(String name, String reason, List<PlacePlan> places) {
            return new CoursePlan(name, reason, places);
        }
    }

    public record PlacePlan(
        String contentId,
        String reason
    ) {

        public static PlacePlan of(String contentId, String reason) {
            return new PlacePlan(contentId, reason);
        }
    }
}
