package live.lbtrip.domain.user.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import live.lbtrip.domain.user.model.Gender;

public record UserUpdateRequest(
    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.name.required}")
    @Size(max = 50, message = "{validation.name.size}")
    String name,

    @Schema(description = "생년월일", example = "1995-05-20", type = "string", format = "date", requiredMode = REQUIRED)
    @NotNull(message = "{validation.birthDate.required}")
    @PastOrPresent(message = "{validation.birthDate.past}")
    LocalDate birthDate,

    @Schema(description = "성별. MALE, FEMALE, NOT_SPECIFIED 중 하나", example = "NOT_SPECIFIED", requiredMode = REQUIRED)
    @NotNull(message = "{validation.gender.required}")
    Gender gender,

    @Schema(description = "영문과 숫자를 포함한 8자 이상의 새 비밀번호. 값이 없으면 변경하지 않습니다.", example = "newpassword123")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "{validation.password.pattern}"
    )
    String password,

    @Schema(description = "새 비밀번호 확인값. 비밀번호 변경 시 필수입니다.", example = "newpassword123")
    String passwordConfirm
) {
    @AssertTrue(message = "{validation.passwordConfirm.mismatch}")
    private boolean isPasswordConfirmed() {
        return password == null || Objects.equals(password, passwordConfirm);
    }
}
