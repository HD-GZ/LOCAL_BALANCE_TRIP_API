package live.lbtrip.global.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldErrorDetail(
    @Schema(description = "validation 실패 필드명", example = "email")
    String field,

    @Schema(description = "validation 실패 메시지", example = "이메일 형식이 올바르지 않습니다.")
    String message
) {
    public static FieldErrorDetail of(String field, String message) {
        return new FieldErrorDetail(field, message);
    }
}
