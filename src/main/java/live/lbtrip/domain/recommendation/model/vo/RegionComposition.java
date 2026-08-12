package live.lbtrip.domain.recommendation.model.vo;

import live.lbtrip.domain.tourism.model.vo.RegionMetrics;

public record RegionComposition(
    RegionMetrics region,
    CourseComposition composition
) {

    public static RegionComposition of(RegionMetrics region, CourseComposition composition) {
        return new RegionComposition(region, composition);
    }
}
