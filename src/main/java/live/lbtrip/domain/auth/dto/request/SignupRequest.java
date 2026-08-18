package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

public record SignupRequest(
    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.name.required}")
    @Size(max = 50, message = "{validation.name.size}")
    String name,

    @Schema(description = "로그인 및 이메일 인증에 사용할 이메일", example = "user@example.com", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Size(max = 255, message = "{validation.email.size}")
    String email,

    @Schema(description = "영문과 숫자를 포함한 8자 이상의 비밀번호", example = "password123", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.password.required}")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "{validation.password.pattern}"
    )
    String password,

    @Schema(description = "비밀번호 확인값", example = "password123", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.passwordConfirm.required}")
    String passwordConfirm,

    @Schema(description = "생년월일", example = "1995-05-20", type = "string", format = "date", requiredMode = REQUIRED)
    @NotNull(message = "{validation.birthDate.required}")
    @PastOrPresent(message = "{validation.birthDate.past}")
    LocalDate birthDate,

    @Schema(description = "성별. MALE, FEMALE, NOT_SPECIFIED 중 하나", example = "NOT_SPECIFIED", requiredMode = REQUIRED)
    @NotNull(message = "{validation.gender.required}")
    Gender gender,

    @Schema(description = "서비스 이용약관 동의 여부. true여야 합니다.", example = "true", requiredMode = REQUIRED)
    @AssertTrue(message = "{validation.termsAgreement.required}")
    boolean termsAgreed,

    @Schema(description = "개인정보 수집 및 이용 동의 여부. true여야 합니다.", example = "true", requiredMode = REQUIRED)
    @AssertTrue(message = "{validation.privacyAgreement.required}")
    boolean privacyAgreed,

    @Schema(description = "마케팅 정보 수신 동의 여부. 선택값입니다.", example = "false")
    boolean marketingAgreed
) {
    public SignupRequest {
        if (!Objects.equals(password, passwordConfirm)) {
            throw BusinessException.of(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
    }
}
