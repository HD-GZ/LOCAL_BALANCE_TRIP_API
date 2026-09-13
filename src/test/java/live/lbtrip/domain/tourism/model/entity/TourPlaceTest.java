package live.lbtrip.domain.tourism.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TourPlaceFixture;

class TourPlaceTest {

    @Test
    void 로케일과_콘텐츠_ID로_생성한다() {
        TourPlace place = TourPlace.create(Locale.ENGLISH, "3093358", RegionCandidateFixture.candidateWithId(), 12,
            "Gwangyang Wine Cave (광양와인동굴)", null, 127.6, 34.9, 0);

        assertThat(place.getLocale()).isEqualTo(Locale.ENGLISH);
        assertThat(place.getContentId()).isEqualTo("3093358");
        assertThat(place.getOverview()).isNull();
        assertThat(place.getTtsAudioKey()).isNull();
        assertThat(place.getTtsSyncedAt()).isNull();
    }

    @Nested
    class TTS_음원_갱신 {

        @Test
        void 음원_키와_동기화_시각을_기록한다() {
            TourPlace place = TourPlaceFixture.withImage("죽녹원", null);
            LocalDateTime syncedAt = LocalDateTime.of(2026, 8, 25, 10, 0);

            place.updateTtsAudio("tts/ko/abc.mp3", syncedAt);

            assertThat(place.getTtsAudioKey()).isEqualTo("tts/ko/abc.mp3");
            assertThat(place.getTtsSyncedAt()).isEqualTo(syncedAt);
        }

        @Test
        void 음원_없음으로_표시하면_키는_비우고_동기화_시각만_기록한다() {
            TourPlace place = TourPlaceFixture.withImage("죽녹원", null);
            LocalDateTime syncedAt = LocalDateTime.of(2026, 8, 25, 10, 0);

            place.markTtsAudioUnavailable(syncedAt);

            assertThat(place.getTtsAudioKey()).isNull();
            assertThat(place.getTtsSyncedAt()).isEqualTo(syncedAt);
        }
    }
}
