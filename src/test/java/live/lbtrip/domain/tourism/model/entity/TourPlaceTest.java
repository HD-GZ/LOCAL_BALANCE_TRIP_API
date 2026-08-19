package live.lbtrip.domain.tourism.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.RegionCandidateFixture;

class TourPlaceTest {

    @Test
    void 로케일과_콘텐츠_ID로_생성한다() {
        TourPlace place = TourPlace.create(Locale.ENGLISH, "3093358", RegionCandidateFixture.candidateWithId(), 12,
            "Gwangyang Wine Cave (광양와인동굴)", null, 127.6, 34.9, 0);

        assertThat(place.getLocale()).isEqualTo(Locale.ENGLISH);
        assertThat(place.getContentId()).isEqualTo("3093358");
        assertThat(place.getOverview()).isNull();
    }
}
