package live.lbtrip.domain.recommendation.service;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.recommendation.model.vo.RegionScoringInput;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.TourContentType;

public enum ScoringAxis {

    LOCALITY(
        propensity -> propensity.getPreference().getLocality(),
        List.of(
            RegionScoringInput::recentOutsiderVisitors,
            input -> input.stats().typeRatio(TourContentType.TOURIST_SPOT.getCode())),
        List.of(input -> -input.stats().totalCount())),

    FRUGALITY(
        propensity -> propensity.getPreference().getFrugality(),
        List.of(input -> input.stats().groupRatio(CategoryGroup.LUXURY_SHOPPING)),
        List.of(input -> input.stats().groupRatio(CategoryGroup.TRADITIONAL_MARKET))),

    EXPERIENTIALITY(
        propensity -> propensity.getPreference().getExperientiality(),
        List.of(input -> input.stats().groupRatio(CategoryGroup.VIEWING_PLACE)),
        List.of(input -> input.stats().groupRatio(CategoryGroup.EXPERIENCE_PLACE))),

    VITALITY(
        propensity -> propensity.getPreference().getVitality(),
        List.of(input -> input.stats().groupRatio(CategoryGroup.NATURE_REST)),
        List.of(input -> input.stats().typeRatio(TourContentType.LEPORTS.getCode()))),

    ACCOMMODATION(
        propensity -> propensity.getValueConsumption().getAccommodation(),
        List.of(),
        List.of(input -> input.stats().typeRatio(TourContentType.ACCOMMODATION.getCode()))),

    FOOD(
        propensity -> propensity.getValueConsumption().getFood(),
        List.of(),
        List.of(input -> input.stats().typeRatio(TourContentType.RESTAURANT.getCode())
            - input.stats().groupRatio(CategoryGroup.CAFE))),

    EXPERIENCE(
        propensity -> propensity.getValueConsumption().getExperience(),
        List.of(),
        List.of(input -> input.stats().groupRatio(CategoryGroup.EXPERIENCE_PLACE)
            + input.stats().typeRatio(TourContentType.LEPORTS.getCode()))),

    CAFE_EXHIBITION(
        propensity -> propensity.getValueConsumption().getCafeExhibition(),
        List.of(),
        List.of(input -> input.stats().groupRatio(CategoryGroup.CAFE)
            + input.stats().groupRatio(CategoryGroup.EXHIBITION)));

    private static final int NEUTRAL_SCORE = 3;

    private final ToIntFunction<Propensity> scoreExtractor;
    private final List<ToDoubleFunction<RegionScoringInput>> leftComponents;
    private final List<ToDoubleFunction<RegionScoringInput>> rightComponents;

    ScoringAxis(
        ToIntFunction<Propensity> scoreExtractor,
        List<ToDoubleFunction<RegionScoringInput>> leftComponents,
        List<ToDoubleFunction<RegionScoringInput>> rightComponents
    ) {
        this.scoreExtractor = scoreExtractor;
        this.leftComponents = leftComponents;
        this.rightComponents = rightComponents;
    }

    public double weight(Propensity propensity) {
        return scoreExtractor.applyAsInt(propensity) - NEUTRAL_SCORE;
    }

    public List<ToDoubleFunction<RegionScoringInput>> leftComponents() {
        return leftComponents;
    }

    public List<ToDoubleFunction<RegionScoringInput>> rightComponents() {
        return rightComponents;
    }

    public boolean isBipolar() {
        return !leftComponents.isEmpty();
    }
}
