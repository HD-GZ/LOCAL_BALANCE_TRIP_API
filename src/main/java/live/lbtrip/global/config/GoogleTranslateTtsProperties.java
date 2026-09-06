package live.lbtrip.global.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tts.google-translate")
public record GoogleTranslateTtsProperties(
    String baseUrl,
    Duration connectTimeout,
    Duration readTimeout,
    Duration requestInterval
) {
}
