package live.lbtrip.domain.recommendation.service;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.global.config.RecommendationProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CourseComposer {

    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;
    private final RecommendationProperties recommendationProperties;

    public CourseComposer(
        ChatClient.Builder chatClientBuilder,
        @Value("classpath:prompts/course-composition.st") Resource promptResource,
        RecommendationProperties recommendationProperties
    ) {
        this.chatClient = chatClientBuilder.build();
        this.promptTemplate = new PromptTemplate(promptResource);
        this.recommendationProperties = recommendationProperties;
    }

    public CourseComposition compose(Propensity propensity, String regionName, List<TourPlace> candidates) {
        try {
            return chatClient.prompt()
                .user(renderPrompt(propensity, regionName, candidates))
                .call()
                .entity(CourseComposition.class);
        } catch (Exception e) {
            log.error("LLM 코스 구성 호출 실패: region={}", regionName, e);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
    }

    private String renderPrompt(Propensity propensity, String regionName, List<TourPlace> candidates) {
        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

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
            Map.entry("candidateLines", candidateLines(candidates)),
            Map.entry("maxCourses", recommendationProperties.maxCourses())
        ));
    }

    private String candidateLines(List<TourPlace> candidates) {
        StringJoiner lines = new StringJoiner("\n");
        for (TourPlace place : candidates) {
            lines.add("%s | %s | %s | %s | %s".formatted(
                place.getContentId(),
                TourContentType.koreanNameOf(place.getContentTypeId()),
                place.getTitle(),
                place.getLongitude(),
                place.getLatitude()
            ));
        }
        return lines.toString();
    }
}
