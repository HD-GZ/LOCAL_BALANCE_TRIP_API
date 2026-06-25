package live.lbtrip.domain.propensity.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "Propensity", description = "6측 취향 진단 (여행지 선택, 소비 기준, 코스 설계, 활동 방식, 여행 강도, 동행 유형)")
public interface PropensityApi {

    String VALIDATION_ERROR_EXAMPLE = """
        {
          "result": "ERROR",
          "data": null,
          "error": {
            "code": "INVALID_INPUT_VALUE",
            "message": "입력값이 올바르지 않습니다.",
            "data": [
              {
                "field": "frugality",
                "message": "소비 기준 형식이 올바르지 않습니다."
              }
            ]
          }
        }
        """;

    String INVALID_ACCESS_TOKEN_EXAMPLE = """
        {
          "result": "ERROR",
          "data": null,
          "error": {
            "code": "INVALID_ACCESS_TOKEN",
            "message": "유효하지 않은 액세스 토큰입니다.",
            "data": null
          }
        }
        """;

    String PROPENSITY_NOT_FOUND_EXAMPLE = """
        {
          "result": "ERROR",
          "data": null,
          "error": {
            "code": "PROPENSITY_NOT_FOUND",
            "message": "취향 진단 결과를 찾을 수 없습니다.",
            "data": null
          }
        }
        """;

    String PROPENSITY_SUCCESS_EXAMPLE = """
        {
          "result": "SUCCESS",
          "data": {
            "result": {
              "type": "실속형 로컬 감성 여행자",
              "description": "럭셔리보다 실속을, 유명 명소보다 골목 상권을, 빡빡한 일정보다 감성 여백을 즐기는 1인 여행자예요."
            },
            "locality": 4,
            "frugality": 5,
            "flexibility": 3,
            "experientiality": 4,
            "vitality": 2,
            "sociality": 1
          },
          "error": null
        }
        """;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "6측 취향 진단 등록",
        description = "6측 취향 진단 결과를 등록하거나 재진단 시 기존 결과를 덮어씁니다. 진단 유형 라벨/설명과 6축 점수를 함께 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "진단 결과 등록 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = PROPENSITY_SUCCESS_EXAMPLE)
        )),
        @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE)
        )),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 access token", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = INVALID_ACCESS_TOKEN_EXAMPLE)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<PropensityResponse>> setPropensity(
        @UserId Long userId,
        @Valid @RequestBody PropensityRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "6측 취향 진단 결과 조회",
        description = "현재 로그인한 사용자의 진단 유형 라벨/설명과 6축 점수를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "진단 결과 조회 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = PROPENSITY_SUCCESS_EXAMPLE)
        )),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 access token", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = INVALID_ACCESS_TOKEN_EXAMPLE)
        )),
        @ApiResponse(responseCode = "404", description = "진단 결과 없음", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = PROPENSITY_NOT_FOUND_EXAMPLE)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<PropensityResponse>> getPropensity(
        @UserId Long userId
    );
}
