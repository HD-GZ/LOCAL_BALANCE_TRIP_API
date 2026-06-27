package live.lbtrip.global.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorMessage(
    @Schema(description = "에러 코드", example = "INVALID_INPUT_VALUE")
    String code,

    @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다.")
    String message,

    @Schema(description = "에러 부가 데이터. validation 실패 시 FieldErrorDetail 배열이 포함될 수 있습니다.")
    Object data
) {
    public static ErrorMessage of(ErrorCode errorCode) {
        return of(errorCode, null);
    }

    public static ErrorMessage of(ErrorCode errorCode, Object data) {
        return new ErrorMessage(errorCode.name(), errorCode.getMessage(), data);
    }
}
