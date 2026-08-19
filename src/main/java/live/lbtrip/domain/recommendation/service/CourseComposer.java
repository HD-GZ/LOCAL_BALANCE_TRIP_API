package live.lbtrip.domain.recommendation.service;

import java.util.List;
import java.util.Locale;
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
import live.lbtrip.domain.recommendation.model.vo.WalkableCluster;
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
    private final PromptTemplate koreanPromptTemplate;
    private final PromptTemplate englishPromptTemplate;
    private final RecommendationProperties recommendationProperties;
    private final CourseCompositionValidator courseCompositionValidator;

    public CourseComposer(
        ChatClient.Builder chatClientBuilder,
        @Value("classpath:prompts/course-composition.st") Resource koreanPromptResource,
        @Value("classpath:prompts/course-composition-en.st") Resource englishPromptResource,
        RecommendationProperties recommendationProperties,
        CourseCompositionValidator courseCompositionValidator
    ) {
        this.chatClient = chatClientBuilder.build();
        this.koreanPromptTemplate = new PromptTemplate(koreanPromptResource);
        this.englishPromptTemplate = new PromptTemplate(englishPromptResource);
        this.recommendationProperties = recommendationProperties;
        this.courseCompositionValidator = courseCompositionValidator;
    }

    public CourseComposition compose(
        Propensity propensity, String regionName, List<WalkableCluster> clusters, Locale locale
    ) {
        CourseComposition raw;
        try {
            raw = chatClient.prompt()
                .user(renderPrompt(propensity, regionName, clusters, locale))
                .call()
                .entity(CourseComposition.class);
        } catch (Exception e) {
            log.error("LLM 코스 구성 호출 실패: region={}", regionName, e);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        return courseCompositionValidator.validate(raw, clusters, regionName);
    }

    private String renderPrompt(
        Propensity propensity, String regionName, List<WalkableCluster> clusters, Locale locale
    ) {
        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

        return promptTemplateFor(locale).render(Map.ofEntries(
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
            Map.entry("candidateLines", candidateLines(clusters, locale)),
            Map.entry("maxCourses", recommendationProperties.maxCourses())
        ));
    }

    private PromptTemplate promptTemplateFor(Locale locale) {
        return isEnglish(locale) ? englishPromptTemplate : koreanPromptTemplate;
    }

    private String candidateLines(List<WalkableCluster> clusters, Locale locale) {
        String clusterLabel = isEnglish(locale) ? "## Cluster %s" : "## 클러스터 %s";
        StringJoiner lines = new StringJoiner("\n");
        for (WalkableCluster cluster : clusters) {
            lines.add(clusterLabel.formatted(cluster.id()));
            for (TourPlace place : cluster.places()) {
                lines.add("%s | %s | %s | %s | %s".formatted(
                    place.getContentId(),
                    TourContentType.nameOf(place.getContentTypeId(), locale),
                    place.getTitle(),
                    place.getLongitude(),
                    place.getLatitude()
                ));
            }
        }
        return lines.toString();
    }

    private boolean isEnglish(Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
    }
}
