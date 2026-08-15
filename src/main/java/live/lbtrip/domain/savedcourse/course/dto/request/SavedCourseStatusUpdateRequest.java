package live.lbtrip.domain.savedcourse.course.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;

public record SavedCourseStatusUpdateRequest(
    @Schema(description = "변경할 여행 상태(BEFORE_TRIP: 여행전, TRAVELING: 여행중, COMPLETED: 완주)", example = "TRAVELING", requiredMode = REQUIRED)
    @NotNull(message = "여행 상태는 필수입니다.")
    SavedCourseStatus status
) {
}
