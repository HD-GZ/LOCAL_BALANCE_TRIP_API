package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;

/**
 * 사용자 성향 8축과 지역 지표로 추천 지역 상위 N개를 선정한다.
 *
 * <pre>
 * 지역 점수 = Σ (8개 축) w × indicator
 *   w         = 축 점수 − 3          (−2 ~ +2, 3점(중립)이면 그 축은 영향 없음)
 *   indicator = [0, 1] 범위의 축별 지표 (후보 집합 상대 min-max 정규화)
 * </pre>
 *
 * 축은 두 종류다.
 * <ul>
 *   <li>단극 축 (숙소·음식·체험·카페전시): indicator = norm(원시값)</li>
 *   <li>양극 축 (로컬·실속·생활체험·활동형): indicator = norm(norm(오른쪽 성분) − norm(왼쪽 성분)).
 *       1점은 왼쪽 극, 5점은 오른쪽 극을 뜻하므로 가중치 부호(w)가 방향을 결정한다.
 *       차이를 다시 정규화하는 이유는 한쪽 성분이 퇴화해도 지표 스윙이 [0,1] 전체를
 *       사용하게 만들어 8개 축의 영향력을 동일하게 유지하기 위함이다.</li>
 * </ul>
 *
 * 모든 정규화는 이번 호출의 후보 집합 안에서만 상대 평가되며, 동점은 입력 순서를 유지한다.
 */
@Component
public class RegionSelector {

    private static final int NEUTRAL_SCORE = 3;

    public List<RegionMetrics> selectTop(Propensity propensity, List<RegionMetrics> metrics, int limit) {
        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

        double[] scores = new double[metrics.size()];
        accumulate(scores, weight(preference.getLocality()), localityIndicator(metrics));
        accumulate(scores, weight(preference.getFrugality()), frugalityIndicator(metrics));
        accumulate(scores, weight(preference.getExperientiality()), experientialityIndicator(metrics));
        accumulate(scores, weight(preference.getVitality()), vitalityIndicator(metrics));
        accumulate(scores, weight(consumption.getAccommodation()), accommodationIndicator(metrics));
        accumulate(scores, weight(consumption.getFood()), foodIndicator(metrics));
        accumulate(scores, weight(consumption.getExperience()), experienceIndicator(metrics));
        accumulate(scores, weight(consumption.getCafeExhibition()), cafeExhibitionIndicator(metrics));

        return topByScore(metrics, scores, limit);
    }

    /**
     * 핫플(1) ↔ 로컬(5) 축.
     * 왼쪽(핫플) = 최근 30일 외지인 방문자 합과 관광지(12) 비율을 각각 정규화해 평균,
     * 오른쪽(로컬) = 희소성(−totalCount). 방문자 데이터가 없는 지역은 0으로 들어가
     * 관광지 비율이 백업 신호로 동작한다.
     */
    private double[] localityIndicator(List<RegionMetrics> metrics) {
        double[] visitors = new double[metrics.size()];
        double[] touristSpotRatios = new double[metrics.size()];
        double[] scarcities = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            RegionMetrics region = metrics.get(i);
            visitors[i] = region.recentOutsiderVisitors();
            touristSpotRatios[i] = region.typeRatio(TourContentType.TOURIST_SPOT.getCode());
            scarcities[i] = -region.totalCount();
        }
        double[] left = average(normalize(visitors), normalize(touristSpotRatios));
        return contrast(left, normalize(scarcities));
    }

    /** 럭셔리(1) ↔ 실속(5) 축. 왼쪽 = 백화점·면세점 비율, 오른쪽 = 5일장·상설시장·특산물판매점 비율. */
    private double[] frugalityIndicator(List<RegionMetrics> metrics) {
        double[] luxuries = new double[metrics.size()];
        double[] markets = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            luxuries[i] = metrics.get(i).groupRatio(CategoryGroup.LUXURY_SHOPPING);
            markets[i] = metrics.get(i).groupRatio(CategoryGroup.TRADITIONAL_MARKET);
        }
        return contrast(normalize(luxuries), normalize(markets));
    }

    /** 관람형(1) ↔ 생활체험(5) 축. 왼쪽 = 문화시설·역사관광지·건축/조형물 비율, 오른쪽 = 체험관광지·공예/공방 비율. */
    private double[] experientialityIndicator(List<RegionMetrics> metrics) {
        double[] viewings = new double[metrics.size()];
        double[] experiences = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            viewings[i] = metrics.get(i).groupRatio(CategoryGroup.VIEWING_PLACE);
            experiences[i] = metrics.get(i).groupRatio(CategoryGroup.EXPERIENCE_PLACE);
        }
        return contrast(normalize(viewings), normalize(experiences));
    }

    /** 휴식형(1) ↔ 활동형(5) 축. 왼쪽 = 자연·휴양관광지 비율, 오른쪽 = 레포츠(28) 비율. */
    private double[] vitalityIndicator(List<RegionMetrics> metrics) {
        double[] rests = new double[metrics.size()];
        double[] leports = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            rests[i] = metrics.get(i).groupRatio(CategoryGroup.NATURE_REST);
            leports[i] = metrics.get(i).typeRatio(TourContentType.LEPORTS.getCode());
        }
        return contrast(normalize(rests), normalize(leports));
    }

    /** 숙소 축. 숙박(32) 비율. */
    private double[] accommodationIndicator(List<RegionMetrics> metrics) {
        double[] values = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            values[i] = metrics.get(i).typeRatio(TourContentType.ACCOMMODATION.getCode());
        }
        return normalize(values);
    }

    /** 음식 축. 음식점(39) 비율에서 카페 비율을 빼 식사 중심 지역을 가려낸다. */
    private double[] foodIndicator(List<RegionMetrics> metrics) {
        double[] values = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            RegionMetrics region = metrics.get(i);
            values[i] = region.typeRatio(TourContentType.RESTAURANT.getCode())
                - region.groupRatio(CategoryGroup.CAFE);
        }
        return normalize(values);
    }

    /** 체험 소비 축. 체험시설 비율 + 레포츠(28) 비율. */
    private double[] experienceIndicator(List<RegionMetrics> metrics) {
        double[] values = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            RegionMetrics region = metrics.get(i);
            values[i] = region.groupRatio(CategoryGroup.EXPERIENCE_PLACE)
                + region.typeRatio(TourContentType.LEPORTS.getCode());
        }
        return normalize(values);
    }

    /** 카페·전시 소비 축. 카페 비율 + 박물관·미술관·전시관 비율. */
    private double[] cafeExhibitionIndicator(List<RegionMetrics> metrics) {
        double[] values = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            RegionMetrics region = metrics.get(i);
            values[i] = region.groupRatio(CategoryGroup.CAFE)
                + region.groupRatio(CategoryGroup.EXHIBITION);
        }
        return normalize(values);
    }

    /**
     * 양극 축 지표: (오른쪽 − 왼쪽) 차이 배열을 다시 [0,1]로 정규화한다.
     * 입력 두 배열은 이미 정규화된 상태여야 한다.
     */
    private double[] contrast(double[] left, double[] right) {
        double[] differences = new double[left.length];
        for (int i = 0; i < differences.length; i++) {
            differences[i] = right[i] - left[i];
        }
        return normalize(differences);
    }

    private double[] average(double[] first, double[] second) {
        double[] averages = new double[first.length];
        for (int i = 0; i < averages.length; i++) {
            averages[i] = (first[i] + second[i]) / 2;
        }
        return averages;
    }

    /**
     * min-max 정규화. 결과는 [0,1]이며 후보 집합 안에서의 상대 위치를 뜻한다.
     * 전 지역이 같은 값이면(range 0) 변별력이 없으므로 전부 0을 반환한다.
     */
    private double[] normalize(double[] values) {
        double[] normalized = new double[values.length];
        if (values.length == 0) {
            return normalized;
        }

        double min = values[0];
        double max = values[0];
        for (double value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double range = max - min;
        if (range == 0) {
            return normalized;
        }
        for (int i = 0; i < values.length; i++) {
            normalized[i] = (values[i] - min) / range;
        }
        return normalized;
    }

    private double weight(int score) {
        return score - NEUTRAL_SCORE;
    }

    private void accumulate(double[] scores, double weight, double[] indicator) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += weight * indicator[i];
        }
    }

    /** 점수 내림차순 상위 limit개. 안정 정렬이므로 동점은 입력 순서를 유지한다. */
    private List<RegionMetrics> topByScore(List<RegionMetrics> metrics, double[] scores, int limit) {
        Integer[] indexes = new Integer[metrics.size()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, (left, right) -> Double.compare(scores[right], scores[left]));

        List<RegionMetrics> top = new ArrayList<>();
        for (int i = 0; i < indexes.length && i < limit; i++) {
            top.add(metrics.get(indexes[i]));
        }
        return List.copyOf(top);
    }
}
