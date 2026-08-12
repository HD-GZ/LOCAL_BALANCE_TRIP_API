package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.config.RecommendationProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM 코스 구성 응답을 신뢰할 수 없는 외부 입력으로 취급해 서버 규칙으로 정제한다.
 * 프롬프트의 지시(후보만 사용, 3~5곳, 코스 간 중복 금지 등)는 LLM이 어길 수 있으므로
 * 여기서 다시 강제하며, 검증을 통과한 결과만 다음 단계로 나간다.
 *
 * <p>검증은 세 층으로 나뉜다.
 * <ul>
 *   <li>장소: 후보 목록에 실존하는 contentId만 채택(환각 차단), 코스 내·코스 간 중복 제거,
 *       코스당 최대 5곳 절단. 정제 후 3곳 미만이면 그 코스는 탈락</li>
 *   <li>코스 구조: 이름·이유 필수(없으면 코스 탈락)와 길이 절단(100자/300자),
 *       이름이 지역명으로 시작하지 않으면 지역명 접두, 코스 수는 maxCourses 상한 초과분 버림</li>
 *   <li>지역 수준: 코스 없음 / 지역 추천 이유 없음 / 전 코스 탈락이면
 *       이 지역의 응답 전체를 무효로 보고 예외를 던진다</li>
 * </ul>
 *
 * <p>실패 정책: 코스 수준 결함은 해당 코스만 버리고(부분 수용), 지역 수준 결함은
 * 예외로 알린다. 이 예외는 RegionCompositionAssembler가 잡아 해당 지역만 건너뛴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseCompositionValidator {

    private static final int MIN_PLACES_PER_COURSE = 3;
    private static final int MAX_PLACES_PER_COURSE = 5;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int REASON_MAX_LENGTH = 300;

    private final RecommendationProperties recommendationProperties;

    public CourseComposition validate(CourseComposition raw, List<TourPlace> candidates, String regionName) {
        if (raw == null || raw.courses() == null || raw.courses().isEmpty()) {
            throw generationFailed(regionName, "LLM 응답에 코스 없음");
        }

        String regionReason = normalizeRequired(raw.regionReason(), REASON_MAX_LENGTH);
        if (regionReason == null) {
            throw generationFailed(regionName, "LLM 응답에 지역 추천 이유 없음");
        }

        Set<String> validIds = new HashSet<>();
        for (TourPlace candidate : candidates) {
            validIds.add(candidate.getContentId());
        }

        Set<String> usedIds = new HashSet<>();
        List<CoursePlan> courses = new ArrayList<>();
        for (CoursePlan course : raw.courses()) {
            if (course == null || courses.size() == recommendationProperties.maxCourses()) {
                continue;
            }
            CoursePlan normalized = normalizeCourse(course, validIds, usedIds, regionName);
            if (normalized != null) {
                courses.add(normalized);
                usedIds.addAll(normalized.placeContentIds());
            }
        }

        if (courses.isEmpty()) {
            throw generationFailed(regionName, "LLM 코스가 검증에서 전부 탈락");
        }
        return CourseComposition.of(regionReason, courses);
    }

    /**
     * 코스 하나를 정제한다. 이름·이유가 비었거나 유효 장소가 3곳 미만이면
     * null을 반환해 이 코스만 탈락시킨다.
     */
    private CoursePlan normalizeCourse(
        CoursePlan course, Set<String> validIds, Set<String> usedIds, String regionName
    ) {
        String name = normalizeRequired(course.name(), NAME_MAX_LENGTH);
        String reason = normalizeRequired(course.reason(), REASON_MAX_LENGTH);
        if (name == null || reason == null) {
            return null;
        }
        if (!name.startsWith(regionName)) {
            name = truncate("%s %s".formatted(regionName, name), NAME_MAX_LENGTH);
        }

        List<String> placeContentIds = selectPlaceContentIds(course.placeContentIds(), validIds, usedIds);
        if (placeContentIds.size() < MIN_PLACES_PER_COURSE) {
            return null;
        }
        return CoursePlan.of(name, reason, placeContentIds);
    }

    /**
     * LLM이 낸 contentId 목록에서 채택 가능한 것만 순서대로 고른다.
     * 후보에 없는 ID(환각)·이미 다른 코스가 쓴 ID(usedIds)·코스 안에서 반복된 ID는
     * 건너뛰고, 5곳이 차면 멈춘다. 코스 간 중복은 앞 코스 우선으로 해소된다.
     */
    private List<String> selectPlaceContentIds(
        List<String> rawIds, Set<String> validIds, Set<String> usedIds
    ) {
        List<String> selected = new ArrayList<>();
        if (rawIds == null) {
            return selected;
        }
        for (String rawId : rawIds) {
            if (selected.size() == MAX_PLACES_PER_COURSE) {
                break;
            }
            if (rawId == null) {
                continue;
            }
            String id = rawId.trim();
            if (!validIds.contains(id) || usedIds.contains(id) || selected.contains(id)) {
                continue;
            }
            selected.add(id);
        }
        return List.copyOf(selected);
    }

    private String normalizeRequired(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return truncate(value.trim(), maxLength);
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private BusinessException generationFailed(String regionName, String message) {
        log.error("{}: region={}", message, regionName);
        return BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
    }
}
