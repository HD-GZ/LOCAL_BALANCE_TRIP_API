package live.lbtrip.domain.recommendation.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SavedCourseListRequest(
    @Parameter(description = "페이지 번호(1부터 시작). 생략 시 1.", example = "1")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    Integer page,

    @Parameter(description = "페이지 크기. 생략 시 4, 최대 50.", example = "4")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다.")
    Integer size
) {
}
