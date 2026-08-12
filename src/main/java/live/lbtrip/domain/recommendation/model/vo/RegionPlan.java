package live.lbtrip.domain.recommendation.model.vo;

import java.util.List;

import live.lbtrip.domain.tourism.model.vo.RegionMetrics;

public record RegionPlan(
    RegionMetrics region,
    String regionReason,
    List<PlannedCourse> courses
) {

    public static RegionPlan of(RegionMetrics region, String regionReason, List<PlannedCourse> courses) {
        return new RegionPlan(region, regionReason, courses);
    }

    public record PlannedCourse(
        String name,
        String reason,
        List<RoutedPlace> places
    ) {

        public static PlannedCourse of(String name, String reason, List<RoutedPlace> places) {
            return new PlannedCourse(name, reason, places);
        }
    }
}
