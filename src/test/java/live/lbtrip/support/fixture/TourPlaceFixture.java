package live.lbtrip.support.fixture;

import java.util.Locale;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.entity.TourPlace;

public final class TourPlaceFixture {

    private TourPlaceFixture() {
    }

    public static TourPlace withImage(String title, String imageUrl) {
        RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
        return TourPlace.create(Locale.KOREAN, "content-" + title.hashCode(), candidate, 12,
            title, imageUrl, 126.9, 35.3, 0);
    }
}
