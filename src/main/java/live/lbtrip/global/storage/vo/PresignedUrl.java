package live.lbtrip.global.storage.vo;

import java.time.LocalDateTime;

public record PresignedUrl(
    String url,
    LocalDateTime expiresAt
) {

    public static PresignedUrl of(String url, LocalDateTime expiresAt) {
        return new PresignedUrl(url, expiresAt);
    }
}
