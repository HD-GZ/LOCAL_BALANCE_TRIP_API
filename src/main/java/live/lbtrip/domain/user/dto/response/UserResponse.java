package live.lbtrip.domain.user.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;

public record UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "사용자 이름", example = "홍길동")
    String name,

    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "생년월일", example = "1995-05-20", type = "string", format = "date")
    LocalDate birthDate,

    @Schema(description = "성별. MALE, FEMALE, NOT_SPECIFIED 중 하나", example = "NOT_SPECIFIED")
    Gender gender,

    @Schema(description = "사용자 상태. 회원가입 직후에는 PENDING_EMAIL_VERIFICATION, 인증 완료 후 ACTIVE입니다.", example = "ACTIVE")
    UserStatus status,

    @Schema(description = "마케팅 정보 수신 동의 여부", example = "false")
    boolean marketingAgreed
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getBirthDate(),
            user.getGender(),
            user.getStatus(),
            user.isMarketingAgreed()
        );
    }
}
