package live.lbtrip.domain.propensity.controller;

import static live.lbtrip.global.error.ErrorCode.INVALID_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.INVALID_INPUT_VALUE;
import static live.lbtrip.global.error.ErrorCode.PROPENSITY_NOT_FOUND;
import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "Propensity", description = "6측 취향 진단 (여행지 선택, 소비 기준, 코스 설계, 활동 방식, 여행 강도, 동행 유형)")
public interface PropensityApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "6측 취향 진단 등록",
        description = "6측 취향 진단 결과를 등록하거나 재진단 시 기존 결과를 덮어씁니다. 진단 유형 라벨/설명과 6축 점수를 함께 반환합니다."
    )
    @ApiSuccessResponse(status = CREATED, description = "진단 결과 등록 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ACCESS_TOKEN
    })
    ResponseEntity<PropensityResponse> setPropensity(
        @UserId Long userId,
        @Valid @RequestBody PropensityRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "6측 취향 진단 결과 조회",
        description = "현재 로그인한 사용자의 진단 유형 라벨/설명과 6축 점수를 조회합니다."
    )
    @ApiSuccessResponse(description = "진단 결과 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        PROPENSITY_NOT_FOUND
    })
    ResponseEntity<PropensityResponse> getPropensity(
        @UserId Long userId
    );
}
