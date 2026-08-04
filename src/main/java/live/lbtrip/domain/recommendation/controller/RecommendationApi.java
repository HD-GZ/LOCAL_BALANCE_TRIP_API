package live.lbtrip.domain.recommendation.controller;

import static live.lbtrip.global.error.ErrorCode.COURSE_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.INVALID_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.PROPENSITY_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.RECOMMENDATION_GENERATION_FAILED;
import static live.lbtrip.global.error.ErrorCode.REGION_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.TOUR_DATA_NOT_READY;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "Recommendation", description = "AI 맞춤 코스 추천 (생성 → 여행지 → 코스 → 상세)")
public interface RecommendationApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "코스 추천 생성",
        description = """
            사용자 성향(10축)과 TourAPI 관광 정보를 기반으로 맞춤 코스 추천을 생성합니다.
            추천 지역 최대 5곳, 지역별 코스 최대 3개와 코스별 장소 스냅샷을 저장합니다.
            생성이 완료되면 기존 추천 결과를 새 결과로 교체합니다.
            외부 API를 순차 호출하므로 수십 초가 걸릴 수 있으며, 완료 후 추천 여행지 목록을 별도로 조회해야 합니다.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "추천 생성 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        PROPENSITY_NOT_FOUND,
        TOUR_DATA_NOT_READY,
        RECOMMENDATION_GENERATION_FAILED
    })
    ResponseEntity<Void> createRecommendations(
        @UserId Long userId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "추천 여행지 리스트 조회",
        description = """
            현재 로그인한 사용자의 추천 여행지 목록을 조회합니다.
            생성 이력이 없으면 빈 리스트를 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "추천 여행지 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN
    })
    ResponseEntity<List<RegionRecommendationResponse>> getRecommendedRegions(
        @UserId Long userId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "여행지별 코스 리스트 조회",
        description = """
            선택한 추천 여행지에 생성된 코스 목록을 조회합니다.
            """
    )
    @ApiSuccessResponse(description = "코스 리스트 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        REGION_NOT_FOUND
    })
    ResponseEntity<List<CourseCandidateResponse>> getRegionCourses(
        @UserId Long userId,
        @Parameter(description = "추천 지역 식별자(서비스 생성 ID)", example = "1")
        @PathVariable Long regionId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "코스 상세 조회",
        description = """
            저장된 장소 스냅샷으로 코스의 장소 타임라인을 조회합니다.
            장소별 소개·이미지·좌표·도보 시간·오디오와 코스 지역에 적용 가능한 혜택 목록을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "코스 상세 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        COURSE_NOT_FOUND
    })
    ResponseEntity<CourseDetailResponse> getCourseDetail(
        @UserId Long userId,
        @Parameter(description = "코스 식별자(서비스 생성 코스 ID)", example = "1")
        @PathVariable Long courseId
    );

}
