package live.lbtrip.domain.savedcourse.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.savedcourse.model.ReceiptOcrResult;

public record ReceiptScanResponse(
    @Schema(description = "저장된 영수증 이미지 키. 증빙 등록 시 그대로 전달.", example = "receipts/2026/07/550e8400-e29b-41d4-a716-446655440000.jpg")
    String imageKey,

    @Schema(description = "영수증 이미지 URL", example = "https://stage.images.lb-trip.live/receipts/2026/07/550e8400-e29b-41d4-a716-446655440000.jpg")
    String imageUrl,

    @Schema(description = "인식된 가맹점명. 인식 실패 시 null.", nullable = true, example = "국수거리 노포")
    String merchantName,

    @Schema(description = "인식된 결제 금액(원). 인식 실패 시 null.", nullable = true, example = "18000")
    Integer amount,

    @Schema(description = "인식된 결제 일자. 인식 실패 시 null.", nullable = true, example = "2026-07-17")
    LocalDate paidDate
) {

    public static ReceiptScanResponse of(String imageKey, String imageUrl, ReceiptOcrResult result) {
        return new ReceiptScanResponse(imageKey, imageUrl, result.merchantName(), result.amount(), result.paidDate());
    }
}
