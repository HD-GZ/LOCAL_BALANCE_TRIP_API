package live.lbtrip.domain.recommendation.service.dto;

import java.util.List;

public record CourseComposition(
    String regionReason,
    List<CoursePlan> courses
) {

    public record CoursePlan(
        String name,
        String reason,
        List<String> placeContentIds
    ) {
    }
}
