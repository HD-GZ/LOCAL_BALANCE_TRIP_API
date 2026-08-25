package live.lbtrip.support.fixture;

import java.time.LocalDate;
import java.util.Locale;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.dto.TourEventItem;
import live.lbtrip.domain.tourism.model.entity.TourEvent;
import live.lbtrip.domain.tourism.model.vo.LocalizedTourEvent;

public final class TourEventFixture {

    public static final String CONTENT_ID = "3021471";
    public static final String TITLE = "담양 대나무축제";
    public static final String IMAGE_URL = "https://img/festival.jpg";
    public static final String ADDRESS = "전라남도 담양군 죽녹원로 119";
    public static final double LATITUDE = 35.3244;
    public static final double LONGITUDE = 126.9816;

    private TourEventFixture() {
    }

    public static TourEvent event(String contentId, LocalDate start, LocalDate end) {
        return event(RegionCandidateFixture.candidateWithId(), Locale.KOREAN, contentId, start, end);
    }

    public static TourEvent event(RegionCandidate candidate, Locale locale, String contentId,
        LocalDate start, LocalDate end) {
        return TourEvent.create(locale, contentId, candidate, TITLE, IMAGE_URL,
            LATITUDE, LONGITUDE, ADDRESS, start, end);
    }

    public static TourEventItem item(String contentId, LocalDate start, LocalDate end) {
        return new TourEventItem(contentId, TITLE, start, end, IMAGE_URL, LONGITUDE, LATITUDE, ADDRESS, null);
    }

    public static LocalizedTourEvent localized(LocalDate start, LocalDate end) {
        return new LocalizedTourEvent(TITLE, IMAGE_URL, start, end, ADDRESS);
    }
}
