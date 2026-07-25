package live.lbtrip.domain.recommendation.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.model.enums.TourContentType;

@Component
public class RegionScorer {

    private static final int NEUTRAL_SCORE = 3;

    public List<RegionStats> selectTop(Propensity propensity, List<RegionStats> statsList, int limit) {
        double[] rarity = normalize(statsList.stream()
            .mapToDouble(stats -> -stats.totalCount()).toArray());
        double[] culture = normalize(ratios(statsList, TourContentType.CULTURAL_FACILITY));
        double[] leports = normalize(ratios(statsList, TourContentType.LEPORTS));
        double[] stay = normalize(ratios(statsList, TourContentType.ACCOMMODATION));
        double[] shopping = normalize(ratios(statsList, TourContentType.SHOPPING));
        double[] food = normalize(ratios(statsList, TourContentType.RESTAURANT));

        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

        double[] scores = new double[statsList.size()];
        for (int i = 0; i < statsList.size(); i++) {
            scores[i] = weight(preference.getLocality()) * rarity[i]
                + weight(preference.getFrugality()) * shopping[i]
                + weight(preference.getExperientiality()) * culture[i]
                + weight(preference.getVitality()) * leports[i]
                + weight(consumption.getFood()) * food[i]
                + weight(consumption.getCafeExhibition()) * culture[i]
                + weight(consumption.getExperience()) * leports[i]
                + weight(consumption.getAccommodation()) * stay[i];
        }

        return IntStream.range(0, statsList.size())
            .boxed()
            .sorted(Comparator.comparingDouble((Integer i) -> scores[i]).reversed())
            .limit(limit)
            .map(statsList::get)
            .toList();
    }

    private double weight(int score) {
        return score - NEUTRAL_SCORE;
    }

    private double[] ratios(List<RegionStats> statsList, TourContentType contentType) {
        return statsList.stream().mapToDouble(stats -> stats.typeRatio(contentType.getCode())).toArray();
    }

    private double[] normalize(double[] values) {
        double min = Arrays.stream(values).min().orElse(0);
        double max = Arrays.stream(values).max().orElse(0);
        double range = max - min;
        double[] normalized = new double[values.length];
        if (range == 0) {
            return normalized;
        }
        for (int i = 0; i < values.length; i++) {
            normalized[i] = (values[i] - min) / range;
        }
        return normalized;
    }
}
