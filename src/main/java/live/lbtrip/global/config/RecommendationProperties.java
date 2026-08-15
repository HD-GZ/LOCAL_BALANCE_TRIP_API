package live.lbtrip.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recommendation")
public record RecommendationProperties(
    int maxRegions,
    int maxCourses,
    List<Integer> walkClusterRadiiMeters
) {
}
