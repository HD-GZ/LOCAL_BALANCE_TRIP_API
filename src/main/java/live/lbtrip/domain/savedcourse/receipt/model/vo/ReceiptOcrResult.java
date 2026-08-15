package live.lbtrip.domain.savedcourse.receipt.model.vo;

import java.time.LocalDate;

public record ReceiptOcrResult(
    String merchantName,
    Integer amount,
    LocalDate paidDate
) {

    public static ReceiptOcrResult empty() {
        return new ReceiptOcrResult(null, null, null);
    }
}
