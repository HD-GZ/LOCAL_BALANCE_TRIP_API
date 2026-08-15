package live.lbtrip.domain.savedcourse.receipt.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.global.storage.vo.PresignedUrl;

public record TourReceiptDownloadUrlResponse(
    @Schema(description = "영수증 이미지 다운로드 URL. 만료 시각 이후에는 사용할 수 없다.", example = "https://stage-lb-trip-images.s3.ap-northeast-2.amazonaws.com/receipts/2026/07/550e8400-e29b-41d4-a716-446655440000.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256")
    String downloadUrl,

    @Schema(description = "다운로드 URL 만료 시각", example = "2026-08-06T19:10:00")
    LocalDateTime expiresAt
) {

    public static TourReceiptDownloadUrlResponse from(PresignedUrl presignedUrl) {
        return new TourReceiptDownloadUrlResponse(presignedUrl.url(), presignedUrl.expiresAt());
    }
}
