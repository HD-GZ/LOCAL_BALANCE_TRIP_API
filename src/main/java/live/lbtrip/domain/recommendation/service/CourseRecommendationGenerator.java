package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.service.RegionStatsFinder;
import live.lbtrip.domain.tourism.service.RegionVisitorFinder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseRecommendationGenerator {

    private final PropensityFinder propensityFinder;
    private final RegionStatsFinder regionStatsFinder;
    private final RegionVisitorFinder regionVisitorFinder;

    public void generate(Long userId) {
        // init
        Propensity propensity = propensityFinder.findByUserId(userId);
        List<RegionStats> regionStats = regionStatsFinder.findAll();
    }
}
