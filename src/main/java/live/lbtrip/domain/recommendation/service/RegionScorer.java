package live.lbtrip.domain.recommendation.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.recommendation.client.dto.RegionStats;

@Component
public class RegionScorer {

    private static final int CULTURE = 14;
    private static final int LEPORTS = 28;
    private static final int STAY = 32;
    private static final int SHOPPING = 38;
    private static final int FOOD = 39;
    private static final int NEUTRAL_SCORE = 3;

    public List<RegionStats> selectTop(Propensity propensity, List<RegionStats> statsList, int limit) {
        double[] rarity = normalize(statsList.stream()
            .mapToDouble(stats -> -stats.totalCount()).toArray());
        double[] culture = normalize(ratios(statsList, CULTURE));
        double[] leports = normalize(ratios(statsList, LEPORTS));
        double[] stay = normalize(ratios(statsList, STAY));
        double[] shopping = normalize(ratios(statsList, SHOPPING));
        double[] food = normalize(ratios(statsList, FOOD));

        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

        record Scored(RegionStats stats, double score) {
        }

        return IntStream.range(0, statsList.size())
            .mapToObj(i -> new Scored(statsList.get(i),
                weight(preference.getLocality()) * rarity[i]
                    + weight(preference.getFrugality()) * shopping[i]
                    + weight(preference.getExperientiality()) * culture[i]
                    + weight(preference.getVitality()) * leports[i]
                    + weight(consumption.getFood()) * food[i]
                    + weight(consumption.getCafeExhibition()) * culture[i]
                    + weight(consumption.getExperience()) * leports[i]
                    + weight(consumption.getAccommodation()) * stay[i]))
            .sorted(Comparator.comparingDouble(Scored::score).reversed())
            .limit(limit)
            .map(Scored::stats)
            .toList();
    }

    private double weight(int score) {
        return score - NEUTRAL_SCORE;
    }

    private double[] ratios(List<RegionStats> statsList, int contentTypeId) {
        return statsList.stream().mapToDouble(stats -> stats.typeRatio(contentTypeId)).toArray();
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
