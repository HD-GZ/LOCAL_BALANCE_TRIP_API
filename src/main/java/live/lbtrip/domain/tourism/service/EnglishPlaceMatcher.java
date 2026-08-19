package live.lbtrip.domain.tourism.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.client.dto.EnglishPlaceItem;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.util.GeoDistanceCalculator;

@Component
public class EnglishPlaceMatcher {

    private static final double FALLBACK_RADIUS_METERS = 1_000;
    private static final Pattern KOREAN_IN_PARENTHESES = Pattern.compile("\\(([^()]*[가-힣][^()]*)\\)\\s*$");

    public Optional<TourPlace> match(EnglishPlaceItem item, List<TourPlace> candidates) {
        return matchByTitle(item, candidates).or(() -> matchByDistance(item, candidates));
    }

    private Optional<TourPlace> matchByTitle(EnglishPlaceItem item, List<TourPlace> candidates) {
        String koreanTitle = extractKoreanTitle(item.title());
        if (koreanTitle == null) {
            return Optional.empty();
        }
        return candidates.stream()
            .filter(place -> koreanTitle.equals(normalize(place.getTitle())))
            .min(Comparator.comparingDouble(place -> distanceOrMax(item, place)));
    }

    private Optional<TourPlace> matchByDistance(EnglishPlaceItem item, List<TourPlace> candidates) {
        return candidates.stream()
            .filter(place -> distanceOrMax(item, place) <= FALLBACK_RADIUS_METERS)
            .min(Comparator.comparingDouble(place -> distanceOrMax(item, place)));
    }

    private double distanceOrMax(EnglishPlaceItem item, TourPlace place) {
        Double distance = GeoDistanceCalculator.distanceMeters(
            item.longitude(), item.latitude(), place.getLongitude(), place.getLatitude());
        return distance == null ? Double.MAX_VALUE : distance;
    }

    private String extractKoreanTitle(String title) {
        if (title == null) {
            return null;
        }
        Matcher matcher = KOREAN_IN_PARENTHESES.matcher(title);
        return matcher.find() ? normalize(matcher.group(1)) : null;
    }

    private String normalize(String title) {
        return title == null ? "" : title.replaceAll("\\s", "");
    }
}
