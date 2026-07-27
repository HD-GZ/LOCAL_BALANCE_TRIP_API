package live.lbtrip.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
    S3 s3,
    String cdnBaseUrl,
    DataSize maxImageSize
) {

    public record S3(
        String bucket,
        String region,
        String accessKey,
        String secretKey
    ) {
    }
}
