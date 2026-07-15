package live.lbtrip.domain.recommendation.controller;

import static live.lbtrip.global.error.ErrorCode.COURSE_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.INVALID_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.PROPENSITY_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.RECOMMENDATION_GENERATION_FAILED;
import static live.lbtrip.global.error.ErrorCode.REGION_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.TOUR_API_UNAVAILABLE;
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

@Tag(name = "Recommendation", description = "AI 맞춤 코스 추천 (생성 → 여행지 → 코스 → 상세 → 저장)")
public interface RecommendationApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "코스 추천 생성",
        description = """
            "코스 추천받기" 버튼용. 사용자 성향(10축) 기반으로 TourAPI를 호출해
            추천 지역 5곳과 지역별 코스 3개, 코스별 장소 스냅샷을 생성해 저장합니다.
            기존 추천 결과는 덮어씁니다. 외부 API를 순차 호출하므로 수십 초가 걸릴 수 있습니다.
            응답 바디는 없으며, 완료 후 여행지 리스트 조회 API를 호출하세요.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "추천 생성 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        PROPENSITY_NOT_FOUND,
        TOUR_API_UNAVAILABLE,
        RECOMMENDATION_GENERATION_FAILED
    })
    ResponseEntity<Void> createRecommendations(
        @UserId Long userId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "추천 여행지 리스트 조회",
        description = """
            생성된 추천 지역 카드 목록을 반환합니다(DB 조회 전용).
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
            선택한 추천 여행지에 생성된 코스 카드 목록을 반환합니다(DB 조회 전용).
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
            저장된 장소 스냅샷으로 코스 순서 타임라인(장소별 소개·이미지·좌표·도보 시간·오디오)을
            반환합니다(DB 조회 전용). 코스 지역(법정동 코드)에 매칭되는 적용 가능 혜택 목록(benefits)을
            함께 반환합니다.
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

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "코스 저장",
        description = """
            코스를 사용자의 저장 목록(마이 > SAVE)에 스냅샷으로 복사합니다.
            이미 저장된 코스면 아무것도 하지 않습니다(멱등). 응답 바디는 없습니다.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "코스 저장 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        COURSE_NOT_FOUND
    })
    ResponseEntity<Void> saveCourse(
        @UserId Long userId,
        @Parameter(description = "코스 식별자(서비스 생성 코스 ID)", example = "1")
        @PathVariable Long courseId
    );
}
