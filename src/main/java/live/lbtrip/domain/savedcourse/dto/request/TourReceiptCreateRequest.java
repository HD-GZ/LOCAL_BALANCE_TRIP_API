package live.lbtrip.domain.savedcourse.dto.request;

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
    @NotNull(message = "영수증 이미지 식별자는 필수입니다.")
    @Positive(message = "영수증 이미지 식별자는 0보다 커야 합니다.")
    Long imageId,

    @Schema(description = "가맹점명", example = "국수거리 노포", requiredMode = REQUIRED)
    @NotBlank(message = "가맹점명은 필수입니다.")
    @Size(max = 100, message = "가맹점명은 100자 이하여야 합니다.")
    String merchantName,

    @Schema(description = "결제 금액(원)", example = "18000", requiredMode = REQUIRED)
    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    Integer amount,

    @Schema(description = "결제 일자", example = "2026-07-17", requiredMode = REQUIRED)
    @NotNull(message = "결제 일자는 필수입니다.")
    @PastOrPresent(message = "결제 일자는 미래일 수 없습니다.")
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
