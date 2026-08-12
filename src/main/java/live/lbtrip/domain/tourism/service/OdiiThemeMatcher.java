package live.lbtrip.domain.tourism.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.util.GeoDistanceCalculator;

/**
 * 장소와 오디오 테마를 짝짓는다. 두 조건을 모두 만족해야 매칭된다.
 * <ul>
 *   <li>거리 500m 이내 — 이름만 같은 다른 지역의 동명 장소 오탐을 막는다</li>
 *   <li>공백 제거한 제목의 상호 포함 — 바로 옆 다른 명소의 오디오 오탐을 막는다</li>
 * </ul>
 * 테마 목록 순서상 먼저 조건을 만족한 테마를 채택하며, 매칭 실패는 정상 상태다
 * (오디오 없는 장소로 저장된다).
 */
@Component
public class OdiiThemeMatcher {

    private static final double MATCH_RADIUS_METERS = 500;

    public Optional<OdiiTheme> match(TourPlace place, List<OdiiTheme> themes) {
        String placeTitle = normalizeTitle(place.getTitle());
        for (OdiiTheme theme : themes) {
            Double distance = GeoDistanceCalculator.distanceMeters(
                place.getLongitude(), place.getLatitude(), theme.getLongitude(), theme.getLatitude());
            if (distance == null || distance > MATCH_RADIUS_METERS) {
                continue;
            }
            String themeTitle = normalizeTitle(theme.getTitle());
            if (placeTitle.contains(themeTitle) || themeTitle.contains(placeTitle)) {
                return Optional.of(theme);
            }
        }
        return Optional.empty();
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.replaceAll("\\s", "");
    }
}
