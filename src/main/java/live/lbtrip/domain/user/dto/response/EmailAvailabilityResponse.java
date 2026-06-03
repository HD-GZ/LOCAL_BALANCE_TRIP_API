package live.lbtrip.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmailAvailabilityResponse(
    @Schema(description = "이메일 사용 가능 여부. true이면 가입에 사용할 수 있습니다.", example = "true")
    boolean available
) {
    public static EmailAvailabilityResponse of(boolean available) {
        return new EmailAvailabilityResponse(available);
    }
}
