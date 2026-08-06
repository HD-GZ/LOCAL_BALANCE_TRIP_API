package live.lbtrip.domain.savedcourse.receipt.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.savedcourse.receipt.model.vo.ReceiptOcrResult;

public record ReceiptScanResponse(
    @Schema(description = "저장된 영수증 이미지 식별자. 증빙 등록 시 그대로 전달.", example = "1")
    Long imageId,

    @Schema(description = "영수증 이미지 조회 URL. 만료 시간이 있는 presigned URL이므로 저장하지 말고 조회 시마다 새로 받아야 한다.", example = "https://stage-lb-trip-images.s3.ap-northeast-2.amazonaws.com/receipts/2026/07/550e8400-e29b-41d4-a716-446655440000.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256")
    String imageUrl,

    @Schema(description = "인식된 가맹점명. 인식 실패 시 null.", nullable = true, example = "국수거리 노포")
    String merchantName,

    @Schema(description = "인식된 결제 금액(원). 인식 실패 시 null.", nullable = true, example = "18000")
    Integer amount,

    @Schema(description = "인식된 결제 일자. 인식 실패 시 null.", nullable = true, example = "2026-07-17")
    LocalDate paidDate
) {

    public static ReceiptScanResponse of(Long imageId, String imageUrl, ReceiptOcrResult result) {
        return new ReceiptScanResponse(imageId, imageUrl, result.merchantName(), result.amount(), result.paidDate());
    }
}
