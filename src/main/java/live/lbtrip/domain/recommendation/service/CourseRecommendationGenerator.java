package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.domain.tourism.service.RegionMetricsFinder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseRecommendationGenerator {

    private final PropensityFinder propensityFinder;
    private final RegionMetricsFinder regionMetricsFinder;

    public void generate(Long userId) {
        // init
        Propensity propensity = propensityFinder.findByUserId(userId);
        List<RegionMetrics> metrics = regionMetricsFinder.findAllMetrics();

    }
}
