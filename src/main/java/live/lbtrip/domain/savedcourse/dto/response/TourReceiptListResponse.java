package live.lbtrip.domain.savedcourse.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.savedcourse.model.entity.TourReceipt;

public record TourReceiptListResponse(
    @Schema(description = "증빙 금액 합계(예상 환급 기준 금액, 원)", example = "52000")
    int totalAmount,

    @Schema(description = "환급 증빙 목록(최신순)")
    List<InnerReceiptResponse> receipts
) {

    public record InnerReceiptResponse(
        @Schema(description = "환급 증빙 식별자", example = "1")
        Long receiptId,

        @Schema(description = "가맹점명", example = "국수거리 노포")
        String merchantName,

        @Schema(description = "결제 금액(원)", example = "18000")
        int amount,

        @Schema(description = "결제 일자", example = "2026-07-17")
        LocalDate paidDate
    ) {

        private static InnerReceiptResponse from(TourReceipt receipt) {
            return new InnerReceiptResponse(
                receipt.getId(),
                receipt.getMerchantName(),
                receipt.getAmount(),
                receipt.getPaidDate()
            );
        }
    }

    public static TourReceiptListResponse from(List<TourReceipt> receipts) {
        int totalAmount = receipts.stream().mapToInt(TourReceipt::getAmount).sum();
        return new TourReceiptListResponse(
            totalAmount,
            receipts.stream().map(InnerReceiptResponse::from).toList()
        );
    }
}
