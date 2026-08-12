package live.lbtrip.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recommendation")
public record RecommendationProperties(
    int maxRegions
) {
}
