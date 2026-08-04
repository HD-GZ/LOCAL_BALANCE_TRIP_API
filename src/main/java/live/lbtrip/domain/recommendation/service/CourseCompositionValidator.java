package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.vo.CourseCandidateGroup;
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
        Set<String> validIds = candidates.stream()
            .map(TourPlace::getContentId)
            .collect(Collectors.toSet());
        return validate(raw, Map.of(), validIds, regionName, false);
    }

    public CourseComposition validateGrouped(
        CourseComposition raw,
        List<CourseCandidateGroup> candidateGroups,
        String regionName
    ) {
        Map<String, Set<String>> validIdsByGroup = new HashMap<>();
        for (CourseCandidateGroup group : candidateGroups) {
            validIdsByGroup.put(group.id(), group.candidates().stream()
                .map(TourPlace::getContentId)
                .collect(Collectors.toSet()));
        }
        return validate(raw, validIdsByGroup, Set.of(), regionName, true);
    }

    private CourseComposition validate(
        CourseComposition raw,
        Map<String, Set<String>> validIdsByGroup,
        Set<String> defaultValidIds,
        String regionName,
        boolean requireGroup
    ) {
        if (raw == null || raw.courses() == null || raw.courses().isEmpty()) {
            throw generationFailed(regionName, "LLM 응답에 코스 없음");
        }

        String regionReason = normalizeRequired(raw.regionReason(), REASON_MAX_LENGTH);
        if (regionReason == null) {
            throw generationFailed(regionName, "LLM 응답에 지역 추천 이유 없음");
        }

        Set<String> usedGroupIds = new HashSet<>();
        List<CoursePlan> courses = new ArrayList<>();
        for (CoursePlan course : raw.courses()) {
            if (course == null || courses.size() == MAX_COURSES) {
                continue;
            }
            String groupId = normalizeGroupId(course.candidateGroupId());
            if (requireGroup && (!validIdsByGroup.containsKey(groupId) || usedGroupIds.contains(groupId))) {
                continue;
            }
            Set<String> validIds = requireGroup ? validIdsByGroup.get(groupId) : defaultValidIds;
            CoursePlan normalized = normalizeCourse(course, validIds, regionName, groupId);
            if (normalized != null) {
                courses.add(normalized);
                if (requireGroup) {
                    usedGroupIds.add(groupId);
                }
            }
        }

        if (courses.isEmpty()) {
            throw generationFailed(regionName, "LLM 코스가 검증에서 전부 탈락");
        }
        return CourseComposition.of(regionReason, courses);
    }

    private CoursePlan normalizeCourse(
        CoursePlan course,
        Set<String> validIds,
        String regionName,
        String groupId
    ) {
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
        return CoursePlan.of(groupId, name, reason, placeContentIds);
    }

    private String normalizeGroupId(String value) {
        return value == null ? null : value.trim();
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
