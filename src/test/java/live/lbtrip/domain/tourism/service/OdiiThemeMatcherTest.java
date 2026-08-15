package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.support.fixture.RegionCandidateFixture;

class OdiiThemeMatcherTest {

    private final OdiiThemeMatcher odiiThemeMatcher = new OdiiThemeMatcher();

    @Test
    void 거리와_제목이_모두_맞는_테마를_매칭한다() {
        TourPlace place = place("죽녹원", 126.9861, 35.3244);
        OdiiTheme theme = theme("담양 죽녹원", 126.9865, 35.3246);

        assertThat(odiiThemeMatcher.match(place, List.of(theme))).contains(theme);
    }

    @Test
    void 제목이_맞아도_500m를_넘으면_매칭하지_않는다() {
        TourPlace place = place("죽녹원", 126.9861, 35.3244);
        OdiiTheme farTheme = theme("담양 죽녹원", 126.9861, 35.3344);

        assertThat(odiiThemeMatcher.match(place, List.of(farTheme))).isEmpty();
    }

    @Test
    void 가까워도_제목이_다르면_매칭하지_않는다() {
        TourPlace place = place("죽녹원", 126.9861, 35.3244);
        OdiiTheme otherTheme = theme("관방제림", 126.9865, 35.3246);

        assertThat(odiiThemeMatcher.match(place, List.of(otherTheme))).isEmpty();
    }

    @Test
    void 장소나_테마의_좌표가_없으면_매칭하지_않는다() {
        TourPlace place = place("죽녹원", null, null);
        OdiiTheme theme = theme("죽녹원", 126.9865, 35.3246);

        assertThat(odiiThemeMatcher.match(place, List.of(theme))).isEmpty();
    }

    @Test
    void 공백_차이는_무시하고_제목을_비교한다() {
        TourPlace place = place("죽녹원", 126.9861, 35.3244);
        OdiiTheme theme = theme("죽 녹 원", 126.9865, 35.3246);

        assertThat(odiiThemeMatcher.match(place, List.of(theme))).contains(theme);
    }

    private TourPlace place(String title, Double longitude, Double latitude) {
        return TourPlace.create("100", RegionCandidateFixture.candidateWithId(), 12,
            title, null, longitude, latitude, 1);
    }

    private OdiiTheme theme(String title, Double longitude, Double latitude) {
        return OdiiTheme.create("t1", "l1", title, longitude, latitude);
    }
}
