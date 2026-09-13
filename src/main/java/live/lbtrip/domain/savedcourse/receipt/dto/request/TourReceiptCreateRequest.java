package live.lbtrip.domain.savedcourse.receipt.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TourReceiptCreateRequest(
    @Schema(description = "영수증 스캔 응답으로 받은 이미지 식별자", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "{validation.receiptImageId.required}")
    @Positive(message = "{validation.receiptImageId.positive}")
    Long imageId,

    @Schema(description = "가맹점명", example = "국수거리 노포", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.storeName.required}")
    @Size(max = 100, message = "{validation.storeName.size}")
    String merchantName,

    @Schema(description = "결제 금액(원)", example = "18000", requiredMode = REQUIRED)
    @NotNull(message = "{validation.paymentAmount.required}")
    @Positive(message = "{validation.paymentAmount.positive}")
    Integer amount,

    @Schema(description = "결제 일자", example = "2026-07-17", requiredMode = REQUIRED)
    @NotNull(message = "{validation.paymentDate.required}")
    @PastOrPresent(message = "{validation.paymentDate.past}")
    LocalDate paidDate
) {

    public static TourReceiptCreateRequest of(
        Long imageId,
        String merchantName,
        Integer amount,
        LocalDate paidDate
    ) {
        return new TourReceiptCreateRequest(imageId, merchantName, amount, paidDate);
    }
}
