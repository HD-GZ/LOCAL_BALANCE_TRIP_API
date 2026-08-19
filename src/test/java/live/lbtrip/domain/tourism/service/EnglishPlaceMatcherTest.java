package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.tourism.client.dto.EnglishPlaceItem;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.support.fixture.RegionCandidateFixture;

class EnglishPlaceMatcherTest {

    private final EnglishPlaceMatcher matcher = new EnglishPlaceMatcher();

    @Nested
    class 제목_매칭 {

        @Test
        void 괄호_안_한국어가_장소명과_같으면_매칭한다() {
            TourPlace place = place("광양와인동굴", 127.6086, 34.9613);
            EnglishPlaceItem item = item("Gwangyang Wine Cave (광양와인동굴)", 127.6086, 34.9613);

            assertThat(matcher.match(item, List.of(place))).contains(place);
        }

        @Test
        void 공백_차이는_무시하고_비교한다() {
            TourPlace place = place("홍쌍리 청매실농원", 127.7166, 35.0755);
            EnglishPlaceItem item = item("Cheong Maesil Farm (홍쌍리청매실농원)", 127.7166, 35.0755);

            assertThat(matcher.match(item, List.of(place))).contains(place);
        }

        @Test
        void 제목이_같으면_거리가_멀어도_매칭한다() {
            TourPlace place = place("백운산자연휴양림", 127.5985, 35.0516);
            EnglishPlaceItem item = item("Baegunsan Recreational Forest (백운산자연휴양림)", 127.6100, 35.0700);

            assertThat(matcher.match(item, List.of(place))).contains(place);
        }

        @Test
        void 같은_이름의_장소가_여럿이면_가장_가까운_장소를_고른다() {
            TourPlace far = place("죽녹원", 127.0000, 35.4000);
            TourPlace near = place("죽녹원", 126.9861, 35.3244);
            EnglishPlaceItem item = item("Juknokwon (죽녹원)", 126.9865, 35.3246);

            assertThat(matcher.match(item, List.of(far, near))).contains(near);
        }
    }

    @Nested
    class 좌표_폴백 {

        @Test
        void 괄호가_없으면_1km_이내_가장_가까운_장소로_매칭한다() {
            TourPlace near = place("광양 매화마을", 127.7153, 35.0804);
            TourPlace other = place("광양와인동굴", 127.6086, 34.9613);
            EnglishPlaceItem item = item("Gwangyang Maehwa Village", 127.7160, 35.0810);

            assertThat(matcher.match(item, List.of(other, near))).contains(near);
        }

        @Test
        void 괄호_한국어가_어떤_장소와도_다르면_좌표로_폴백한다() {
            TourPlace near = place("광양매화마을", 127.7153, 35.0804);
            EnglishPlaceItem item = item("Maehwa Village (매화마을 관광지)", 127.7160, 35.0810);

            assertThat(matcher.match(item, List.of(near))).contains(near);
        }

        @Test
        void 가장_가까운_장소가_1km를_넘으면_매칭하지_않는다() {
            TourPlace place = place("광양와인동굴", 127.6086, 34.9613);
            EnglishPlaceItem item = item("Somewhere Else", 127.6086, 34.9800);

            assertThat(matcher.match(item, List.of(place))).isEmpty();
        }

        @Test
        void 좌표가_없으면_매칭하지_않는다() {
            TourPlace place = place("광양와인동굴", 127.6086, 34.9613);
            EnglishPlaceItem item = item("Somewhere Else", null, null);

            assertThat(matcher.match(item, List.of(place))).isEmpty();
        }
    }

    @Test
    void 후보가_비어_있으면_매칭하지_않는다() {
        EnglishPlaceItem item = item("Gwangyang Wine Cave (광양와인동굴)", 127.6086, 34.9613);

        assertThat(matcher.match(item, List.of())).isEmpty();
    }

    private TourPlace place(String title, Double longitude, Double latitude) {
        return TourPlace.create("100", RegionCandidateFixture.candidateWithId(), 12,
            title, null, longitude, latitude, 1);
    }

    private EnglishPlaceItem item(String title, Double longitude, Double latitude) {
        return new EnglishPlaceItem("900", title, longitude, latitude);
    }
}
