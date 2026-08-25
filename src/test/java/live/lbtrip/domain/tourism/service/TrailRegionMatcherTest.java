package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.region.model.RegionCandidate;

class TrailRegionMatcherTest {

    private final RegionCandidate gangwonGoseong = RegionCandidate.create("강원특별자치도 고성군", "51", "820");
    private final RegionCandidate gyeongnamGoseong = RegionCandidate.create("경상남도 고성군", "48", "820");
    private final RegionCandidate jeonbukSunchang = RegionCandidate.create("전북특별자치도 순창군", "52", "770");
    private final RegionCandidate busanSeogu = RegionCandidate.create("부산광역시 서구", "26", "140");

    private final TrailRegionMatcher matcher = new TrailRegionMatcher(
        List.of(gangwonGoseong, gyeongnamGoseong, jeonbukSunchang, busanSeogu));

    @Nested
    class 지역_매칭 {

        @Test
        void 시도가_개편된_이름도_같은_지역으로_매칭한다() {
            assertThat(matcher.match("강원도 고성군")).contains(gangwonGoseong);
            assertThat(matcher.match("전라북도 순창군")).contains(jeonbukSunchang);
        }

        @Test
        void 같은_시군_이름이면_시도로_구분한다() {
            assertThat(matcher.match("경상남도 고성군")).contains(gyeongnamGoseong);
            assertThat(matcher.match("강원특별자치도 고성군")).contains(gangwonGoseong);
        }

        @Test
        void 공백을_정리한_뒤_매칭한다() {
            assertThat(matcher.match("  부산광역시   서구 ")).contains(busanSeogu);
        }

        @Test
        void 시도가_다르면_매칭하지_않는다() {
            assertThat(matcher.match("인천광역시 서구")).isEmpty();
        }

        @Test
        void 값이_없거나_토큰이_부족하면_매칭하지_않는다() {
            assertThat(matcher.match(null)).isEmpty();
            assertThat(matcher.match("고성군")).isEmpty();
        }
    }
}
