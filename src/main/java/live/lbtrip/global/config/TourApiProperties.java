package live.lbtrip.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tour-api")
public record TourApiProperties(
    String baseUrl,
    String odiiBaseUrl,
    String serviceKey
) {
}
