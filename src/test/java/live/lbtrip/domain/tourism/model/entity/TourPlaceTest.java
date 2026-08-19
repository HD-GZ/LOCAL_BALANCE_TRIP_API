package live.lbtrip.domain.tourism.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.TourPlaceFixture;

class TourPlaceTest {

    @Test
    void 영문_장소명과_영문_콘텐츠_ID를_갱신한다() {
        TourPlace place = TourPlaceFixture.withImage("광양와인동굴", "https://image/1");

        place.updateEnglish("3093358", "Gwangyang Wine Cave (광양와인동굴)");

        assertThat(place.getEngContentId()).isEqualTo("3093358");
        assertThat(place.getTitleEn()).isEqualTo("Gwangyang Wine Cave (광양와인동굴)");
        assertThat(place.getOverviewEn()).isNull();
    }

    @Test
    void 영문_소개를_갱신한다() {
        TourPlace place = TourPlaceFixture.withImage("광양와인동굴", "https://image/1");

        place.updateEnglishOverview("Opened in July 2017");

        assertThat(place.getOverviewEn()).isEqualTo("Opened in July 2017");
    }
}
