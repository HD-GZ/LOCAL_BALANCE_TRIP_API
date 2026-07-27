package live.lbtrip.domain.user.dto.request;

import java.time.LocalDate;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

public record UserUpdateRequest(
    @Schema(description = "변경할 사용자 이름. 값이 없으면 변경하지 않습니다.", example = "홍길동")
    @Pattern(regexp = ".*\\S.*", message = "이름은 공백일 수 없습니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    String name,

    @Schema(description = "변경할 생년월일. 값이 없으면 변경하지 않습니다.", example = "1995-05-20", type = "string", format = "date")
    @PastOrPresent(message = "생년월일은 미래 날짜일 수 없습니다.")
    LocalDate birthDate,

    @Schema(description = "변경할 성별. MALE, FEMALE, NOT_SPECIFIED 중 하나. 값이 없으면 변경하지 않습니다.", example = "NOT_SPECIFIED")
    Gender gender,

    @Schema(description = "현재 비밀번호. 비밀번호 변경 시 필수입니다.", example = "password123")
    String currentPassword,

    @Schema(description = "영문과 숫자를 포함한 8자 이상의 새 비밀번호. 값이 없으면 변경하지 않습니다.", example = "newpassword123")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다."
    )
    String newPassword,

    @Schema(description = "새 비밀번호 확인값. 비밀번호 변경 시 필수입니다.", example = "newpassword123")
    String newPasswordConfirm
) {
    public UserUpdateRequest {
        if (newPassword != null) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw BusinessException.of(ErrorCode.CURRENT_PASSWORD_REQUIRED);
            }
            if (!Objects.equals(newPassword, newPasswordConfirm)) {
                throw BusinessException.of(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
            }
        }
    }
}
