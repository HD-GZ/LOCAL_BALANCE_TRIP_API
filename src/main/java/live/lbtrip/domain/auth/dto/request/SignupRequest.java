package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import live.lbtrip.domain.user.model.Gender;

public record SignupRequest(
    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    String name,

    @Schema(description = "로그인 및 이메일 인증에 사용할 이메일", example = "user@example.com", requiredMode = REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    String email,

    @Schema(description = "영문과 숫자를 포함한 8자 이상의 비밀번호", example = "password123", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다."
    )
    String password,

    @Schema(description = "비밀번호 확인값", example = "password123", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    String passwordConfirm,

    @Schema(description = "휴대폰 번호", example = "010-1234-5678", requiredMode = REQUIRED)
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-?\\d{4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    String phoneNumber,

    @Schema(description = "생년월일", example = "1995-05-20", type = "string", format = "date", requiredMode = REQUIRED)
    @NotNull(message = "생년월일은 필수입니다.")
    @PastOrPresent(message = "생년월일은 미래 날짜일 수 없습니다.")
    LocalDate birthDate,

    @Schema(description = "성별. MALE, FEMALE, NOT_SPECIFIED 중 하나", example = "NOT_SPECIFIED", requiredMode = REQUIRED)
    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @Schema(description = "서비스 이용약관 동의 여부. true여야 합니다.", example = "true", requiredMode = REQUIRED)
    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    boolean termsAgreed,

    @Schema(description = "개인정보 수집 및 이용 동의 여부. true여야 합니다.", example = "true", requiredMode = REQUIRED)
    @AssertTrue(message = "개인정보 수집·이용 동의는 필수입니다.")
    boolean privacyAgreed,

    @Schema(description = "마케팅 정보 수신 동의 여부. 선택값입니다.", example = "false")
    boolean marketingAgreed
) {
}
