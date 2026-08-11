package live.lbtrip.domain.recommendation.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.recommendation.model.vo.RegionScoringInput;
import live.lbtrip.domain.tourism.client.dto.RegionStats;

@Component
public class RegionScorer {

    public List<RegionStats> selectTop(Propensity propensity, List<RegionScoringInput> inputs, int limit) {
        double[] scores = new double[inputs.size()];
        for (ScoringAxis axis : ScoringAxis.values()) {
            double weight = axis.weight(propensity);
            if (weight == 0) {
                continue;
            }
            double[] indicator = indicator(axis, inputs);
            for (int i = 0; i < scores.length; i++) {
                scores[i] += weight * indicator[i];
            }
        }

        return IntStream.range(0, inputs.size())
            .boxed()
            .sorted(Comparator.comparingDouble((Integer i) -> scores[i]).reversed())
            .limit(limit)
            .map(i -> inputs.get(i).stats())
            .toList();
    }

    private double[] indicator(ScoringAxis axis, List<RegionScoringInput> inputs) {
        double[] right = averageOfNormalized(axis.rightComponents(), inputs);
        if (!axis.isBipolar()) {
            return right;
        }
        double[] left = averageOfNormalized(axis.leftComponents(), inputs);
        double[] contrast = new double[inputs.size()];
        for (int i = 0; i < contrast.length; i++) {
            contrast[i] = right[i] - left[i];
        }
        return normalize(contrast);
    }

    private double[] averageOfNormalized(
        List<ToDoubleFunction<RegionScoringInput>> components,
        List<RegionScoringInput> inputs
    ) {
        double[] average = new double[inputs.size()];
        for (ToDoubleFunction<RegionScoringInput> component : components) {
            double[] normalized = normalize(inputs.stream().mapToDouble(component).toArray());
            for (int i = 0; i < average.length; i++) {
                average[i] += normalized[i] / components.size();
            }
        }
        return average;
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
