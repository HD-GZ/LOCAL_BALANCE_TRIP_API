package live.lbtrip.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
    S3 s3,
    String cdnBaseUrl
) {

    public record S3(
        String bucket,
        String region,
        String accessKey,
        String secretKey
    ) {
    }
}
