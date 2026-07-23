package live.lbtrip.global.web;

import org.springframework.data.domain.PageRequest;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageQueryRequest(
    @Parameter(description = "페이지 번호(1부터 시작). 생략 시 1.", example = "1")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    Integer page,

    @Parameter(description = "페이지 크기. 생략 시 API 기본값, 최대 50.", example = "4")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다.")
    Integer limit
) {

    private static final int DEFAULT_PAGE = 1;

    public PageRequest toPageRequest(int defaultLimit) {
        int resolvedPage = page != null ? page : DEFAULT_PAGE;
        int resolvedLimit = limit != null ? limit : defaultLimit;
        return PageRequest.of(resolvedPage - 1, resolvedLimit);
    }
}
