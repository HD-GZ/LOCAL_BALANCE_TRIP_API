package live.lbtrip.domain.terms.controller;

import static live.lbtrip.global.error.ErrorCode.TERMS_NOT_FOUND;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.terms.dto.response.TermsResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;

@Tag(name = "Terms", description = "약관 전문 조회 API")
public interface TermsApi {

    @Operation(
        summary = "약관 전문 조회",
        description = """
            약관 종류별로 현재 시행 중인 버전의 전문을 조회합니다.
            시행일이 오늘 이전인 버전 중 가장 최근에 시행된 버전을 반환하며,
            전문은 마크다운 형식입니다.
            """
    )
    @ApiSuccessResponse(description = "약관 전문 조회 성공")
    @ApiErrorCodeResponses(TERMS_NOT_FOUND)
    ResponseEntity<TermsResponse> getTerms(
        @Parameter(
            description = "약관 종류. service(서비스 이용약관), privacy(개인정보 수집·이용 동의), marketing(혜택·여행 소식 알림 수신)",
            example = "service",
            schema = @Schema(allowableValues = {"service", "privacy", "marketing"})
        )
        @PathVariable String type
    );
}
