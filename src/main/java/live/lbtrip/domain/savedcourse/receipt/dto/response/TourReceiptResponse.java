package live.lbtrip.domain.savedcourse.receipt.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;

public record TourReceiptResponse(
    @Schema(description = "환급 증빙 식별자", example = "1")
    Long receiptId,

    @Schema(description = "가맹점명", example = "국수거리 노포")
    String merchantName,

    @Schema(description = "결제 금액(원)", example = "18000")
    int amount,

    @Schema(description = "결제 일자", example = "2026-07-17")
    LocalDate paidDate,

    @Schema(description = "영수증 원본 이미지 조회 URL. 만료 시간이 있는 presigned URL이므로 저장하지 말고 조회 시마다 새로 받아야 한다.", example = "https://stage-lb-trip-images.s3.ap-northeast-2.amazonaws.com/receipts/2026/07/550e8400-e29b-41d4-a716-446655440000.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256")
    String imageUrl
) {

    public static TourReceiptResponse from(TourReceipt receipt, String imageUrl) {
        return new TourReceiptResponse(
            receipt.getId(),
            receipt.getMerchantName(),
            receipt.getAmount(),
            receipt.getPaidDate(),
            imageUrl
        );
    }
}
