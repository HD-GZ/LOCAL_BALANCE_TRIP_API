package live.lbtrip.admin.incentive.controller;

import static live.lbtrip.global.error.ErrorCode.INCENTIVE_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.INCENTIVE_REGION_INVALID;
import static live.lbtrip.global.error.ErrorCode.INVALID_ADMIN_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.INVALID_INCENTIVE_PERIOD;
import static live.lbtrip.global.error.ErrorCode.INVALID_INPUT_VALUE;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.admin.incentive.dto.request.AdminIncentiveRequest;
import live.lbtrip.admin.incentive.dto.response.AdminIncentiveResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.AdminId;

@Tag(name = "Admin Incentive", description = "어드민 인센티브 관리 API")
public interface AdminIncentiveApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "인센티브 등록",
        description = """
            행사 제목, 페이지 URL, 부가 설명, 혜택 시작일, 선택적인 혜택 종료일과 적용 지역(법정동 코드) 목록을 등록합니다.
            종료일이 null이면 종료일 없이 혜택을 유지합니다.
            등록된 인센티브 정보를 반환합니다.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "인센티브 등록 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ADMIN_ACCESS_TOKEN,
        INVALID_INCENTIVE_PERIOD,
        INCENTIVE_REGION_INVALID
    })
    ResponseEntity<AdminIncentiveResponse> createIncentive(
        @AdminId Long adminId,
        @Valid @RequestBody AdminIncentiveRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "인센티브 목록 조회",
        description = """
            등록된 인센티브 전체 목록을 조회합니다.
            각 인센티브의 적용 지역 목록을 함께 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "인센티브 목록 조회 성공")
    @ApiErrorCodeResponses(INVALID_ADMIN_ACCESS_TOKEN)
    ResponseEntity<List<AdminIncentiveResponse>> getIncentives(@AdminId Long adminId);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "인센티브 수정",
        description = """
            행사 제목, 페이지 URL, 부가 설명, 혜택 시작일, 선택적인 혜택 종료일과 적용 지역(법정동 코드) 목록을 수정합니다.
            종료일이 null이면 종료일 없이 혜택을 유지합니다.
            적용 지역 목록은 요청 값으로 전체 교체되며, 수정된 인센티브 정보를 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "인센티브 수정 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ADMIN_ACCESS_TOKEN,
        INVALID_INCENTIVE_PERIOD,
        INCENTIVE_NOT_FOUND,
        INCENTIVE_REGION_INVALID
    })
    ResponseEntity<AdminIncentiveResponse> updateIncentive(
        @AdminId Long adminId,
        @Parameter(description = "인센티브 ID", example = "1") @PathVariable Long incentiveId,
        @Valid @RequestBody AdminIncentiveRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "인센티브 삭제",
        description = """
            인센티브와 연결된 적용 지역 정보를 삭제합니다.
            """
    )
    @ApiSuccessResponse(description = "인센티브 삭제 성공")
    @ApiErrorCodeResponses({
        INVALID_ADMIN_ACCESS_TOKEN,
        INCENTIVE_NOT_FOUND
    })
    ResponseEntity<Void> deleteIncentive(
        @AdminId Long adminId,
        @Parameter(description = "인센티브 ID", example = "1") @PathVariable Long incentiveId
    );
}
