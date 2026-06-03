package live.lbtrip.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import live.lbtrip.domain.user.Gender;

public record SignupRequest(
	@NotBlank(message = "이름은 필수입니다.")
	@Size(max = 50, message = "이름은 50자 이하여야 합니다.")
	String name,

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
	String email,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
		message = "비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다."
	)
	String password,

	@NotBlank(message = "비밀번호 확인은 필수입니다.")
	String passwordConfirm,

	@NotBlank(message = "전화번호는 필수입니다.")
	@Pattern(regexp = "^010-?\\d{4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
	String phoneNumber,

	@NotNull(message = "나이는 필수입니다.")
	@Min(value = 1, message = "나이는 1 이상이어야 합니다.")
	@Max(value = 120, message = "나이는 120 이하여야 합니다.")
	Integer age,

	@NotNull(message = "성별은 필수입니다.")
	Gender gender,

	@AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
	boolean termsAgreed,

	@AssertTrue(message = "개인정보 수집·이용 동의는 필수입니다.")
	boolean privacyAgreed,

	boolean marketingAgreed
) {
}
