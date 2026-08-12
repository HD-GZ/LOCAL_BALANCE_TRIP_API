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
        List<String> placeContentIds
    ) {

        public static CoursePlan of(String name, String reason, List<String> placeContentIds) {
            return new CoursePlan(name, reason, placeContentIds);
        }
    }
}
