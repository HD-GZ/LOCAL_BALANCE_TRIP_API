package live.lbtrip.domain.recommendation.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CourseCompositionValidator {

    private static final int MAX_COURSES = 3;
    private static final int MIN_PLACES_PER_COURSE = 3;
    private static final int MAX_PLACES_PER_COURSE = 5;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int REASON_MAX_LENGTH = 300;

    public CourseComposition validate(
        CourseComposition raw,
        List<TourPlace> candidates,
        String regionName
    ) {
        if (raw == null || raw.courses() == null || raw.courses().isEmpty()) {
            throw generationFailed(regionName, "LLM 응답에 코스 없음");
        }

        String regionReason = normalizeRequired(raw.regionReason(), REASON_MAX_LENGTH);
        if (regionReason == null) {
            throw generationFailed(regionName, "LLM 응답에 지역 추천 이유 없음");
        }

        Set<String> validIds = candidates.stream()
            .map(TourPlace::getContentId)
            .collect(Collectors.toSet());

        List<CoursePlan> courses = raw.courses().stream()
            .filter(Objects::nonNull)
            .map(course -> normalizeCourse(course, validIds, regionName))
            .filter(Objects::nonNull)
            .limit(MAX_COURSES)
            .toList();

        if (courses.isEmpty()) {
            throw generationFailed(regionName, "LLM 코스가 검증에서 전부 탈락");
        }
        return CourseComposition.of(regionReason, courses);
    }

    private CoursePlan normalizeCourse(CoursePlan course, Set<String> validIds, String regionName) {
        String name = normalizeRequired(course.name(), NAME_MAX_LENGTH);
        String reason = normalizeRequired(course.reason(), REASON_MAX_LENGTH);
        if (name == null || reason == null) {
            return null;
        }
        if (!name.startsWith(regionName)) {
            name = truncate("%s %s".formatted(regionName, name), NAME_MAX_LENGTH);
        }

        List<String> placeContentIds = course.placeContentIds() == null
            ? List.of()
            : course.placeContentIds().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(validIds::contains)
                .distinct()
                .limit(MAX_PLACES_PER_COURSE)
                .toList();
        if (placeContentIds.size() < MIN_PLACES_PER_COURSE) {
            return null;
        }
        return CoursePlan.of(name, reason, placeContentIds);
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
