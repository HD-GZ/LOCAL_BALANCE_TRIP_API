package live.lbtrip.global.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tour-api")
public record TourApiProperties(
    String baseUrl,
    String odiiBaseUrl,
    String serviceKey,
    String mobileOs,
    String mobileApp,
    Duration readTimeout
) {
}
