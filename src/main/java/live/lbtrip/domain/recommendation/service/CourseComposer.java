package live.lbtrip.domain.recommendation.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.recommendation.client.dto.TourPlace;
import live.lbtrip.domain.recommendation.service.dto.CourseComposition;
import live.lbtrip.domain.recommendation.service.dto.CourseComposition.CoursePlan;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CourseComposer {

    private static final int MAX_COURSES = 3;
    private static final int MIN_PLACES_PER_COURSE = 2;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int REASON_MAX_LENGTH = 300;
    private static final String UNKNOWN_TYPE_NAME = "기타";

    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;

    public CourseComposer(
        ChatClient.Builder chatClientBuilder,
        @Value("classpath:prompts/course-composition.st") Resource promptResource
    ) {
        this.chatClient = chatClientBuilder.build();
        this.promptTemplate = new PromptTemplate(promptResource);
    }

    public CourseComposition compose(
        Propensity propensity,
        String regionName,
        List<TourPlace> candidates,
        Map<Integer, String> typeNames
    ) {
        CourseComposition raw;
        try {
            raw = chatClient.prompt()
                .user(renderPrompt(propensity, regionName, candidates, typeNames))
                .call()
                .entity(CourseComposition.class);
        } catch (Exception e) {
            log.error("LLM 코스 구성 호출 실패: region={}", regionName, e);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        return validate(raw, candidates, regionName);
    }

    private String renderPrompt(
        Propensity propensity,
        String regionName,
        List<TourPlace> candidates,
        Map<Integer, String> typeNames
    ) {
        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

        String candidateLines = candidates.stream()
            .map(place -> "%s | %s | %s".formatted(
                place.contentId(),
                typeNames.getOrDefault(place.contentTypeId(), UNKNOWN_TYPE_NAME),
                place.title()))
            .collect(Collectors.joining("\n"));

        return promptTemplate.render(Map.ofEntries(
            Map.entry("regionName", regionName),
            Map.entry("locality", preference.getLocality()),
            Map.entry("frugality", preference.getFrugality()),
            Map.entry("experientiality", preference.getExperientiality()),
            Map.entry("vitality", preference.getVitality()),
            Map.entry("sociality", preference.getSociality()),
            Map.entry("accommodation", consumption.getAccommodation()),
            Map.entry("food", consumption.getFood()),
            Map.entry("experience", consumption.getExperience()),
            Map.entry("transportation", consumption.getTransportation()),
            Map.entry("cafeExhibition", consumption.getCafeExhibition()),
            Map.entry("candidateLines", candidateLines),
            Map.entry("maxCourses", MAX_COURSES)
        ));
    }

    private CourseComposition validate(CourseComposition raw, List<TourPlace> candidates, String regionName) {
        if (raw == null || raw.courses() == null || raw.courses().isEmpty()) {
            log.error("LLM 응답에 코스 없음: region={}", regionName);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        Set<String> validIds = candidates.stream().map(TourPlace::contentId).collect(Collectors.toSet());

        List<CoursePlan> courses = raw.courses().stream()
            .map(course -> new CoursePlan(
                truncate(course.name(), NAME_MAX_LENGTH),
                truncate(course.reason(), REASON_MAX_LENGTH),
                course.placeContentIds() == null ? List.<String>of()
                    : course.placeContentIds().stream().filter(validIds::contains).distinct().toList()))
            .filter(course -> course.placeContentIds().size() >= MIN_PLACES_PER_COURSE)
            .limit(MAX_COURSES)
            .toList();

        if (courses.isEmpty()) {
            log.error("LLM 코스가 검증에서 전부 탈락: region={}", regionName);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        return new CourseComposition(truncate(raw.regionReason(), REASON_MAX_LENGTH), courses);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
