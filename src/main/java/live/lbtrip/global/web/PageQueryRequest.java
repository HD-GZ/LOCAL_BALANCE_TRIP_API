package live.lbtrip.global.web;

import org.springframework.data.domain.PageRequest;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageQueryRequest(
    @Parameter(description = "페이지 번호(1부터 시작). 생략 시 1.", example = "1")
    @Min(value = 1, message = "{validation.page.min}")
    Integer page,

    @Parameter(description = "페이지 크기. 생략 시 10, 최대 50.", example = "10")
    @Min(value = 1, message = "{validation.pageSize.min}")
    @Max(value = 50, message = "{validation.pageSize.max}")
    Integer limit
) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;

    public PageRequest toPageRequest() {
        int resolvedPage = page != null ? page : DEFAULT_PAGE;
        int resolvedLimit = limit != null ? limit : DEFAULT_LIMIT;
        return PageRequest.of(resolvedPage - 1, resolvedLimit);
    }
}
