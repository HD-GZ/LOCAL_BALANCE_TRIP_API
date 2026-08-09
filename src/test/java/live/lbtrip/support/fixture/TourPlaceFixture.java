package live.lbtrip.support.fixture;

import live.lbtrip.domain.tourism.model.entity.TourPlace;

public final class TourPlaceFixture {

    private TourPlaceFixture() {
    }

    public static TourPlace withImage(String title, String imageUrl) {
        return TourPlace.create(
            "content-" + title.hashCode(), "46", "710", 12,
            title, imageUrl, 126.9, 35.3, 0);
    }
}
