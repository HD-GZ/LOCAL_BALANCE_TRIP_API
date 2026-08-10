package live.lbtrip.domain.savedcourse.tour.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TourEndRequest(
    @Schema(description = "투어 중 걸은 거리(미터)", example = "8400", requiredMode = REQUIRED)
    @NotNull(message = "걸은 거리는 필수입니다.")
    @PositiveOrZero(message = "걸은 거리는 0 이상이어야 합니다.")
    Integer walkedDistanceMeters
) {

    public static TourEndRequest of(Integer walkedDistanceMeters) {
        return new TourEndRequest(walkedDistanceMeters);
    }
}
