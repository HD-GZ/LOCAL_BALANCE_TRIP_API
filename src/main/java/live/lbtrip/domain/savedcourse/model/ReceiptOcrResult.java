package live.lbtrip.domain.savedcourse.model;

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
