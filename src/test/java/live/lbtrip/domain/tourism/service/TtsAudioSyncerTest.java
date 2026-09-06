package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.tts.Speech;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.tourism.client.GoogleTranslateTtsClient;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.service.AudioStorage;
import live.lbtrip.support.fixture.TourPlaceFixture;

@ExtendWith(MockitoExtension.class)
class TtsAudioSyncerTest {

    private static final byte[] MP3_BYTES = {0x49, 0x44, 0x33};

    @Mock
    private TourPlaceRepository tourPlaceRepository;

    @Mock
    private TextToSpeechModel textToSpeechModel;

    @Mock
    private AudioStorage audioStorage;

    @Mock
    private GoogleTranslateTtsClient googleTranslateTtsClient;

    @InjectMocks
    private TtsAudioSyncer ttsAudioSyncer;

    @Nested
    class 구글_번역_TTS {

        @ParameterizedTest
        @ValueSource(strings = {"ko", "en"})
        void 활성화하면_기존_저장_방식으로_저장하고_OpenAI는_호출하지_않는다(String language) {
            Locale locale = Locale.forLanguageTag(language);
            TourPlace place = TourPlaceFixture.withOverview("장소", "소개");
            String key = "tts/" + language + "/abc.mp3";
            ReflectionTestUtils.setField(ttsAudioSyncer, "googleTranslateTtsEnabled", true);
            when(tourPlaceRepository.findTtsPending(eq(locale), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(place))).thenReturn(Page.empty());
            when(googleTranslateTtsClient.synthesize("소개", locale)).thenReturn(MP3_BYTES);
            when(audioStorage.storeTts(MP3_BYTES, locale)).thenReturn(key);

            ttsAudioSyncer.sync(locale);

            assertThat(place.getTtsAudioKey()).isEqualTo(key);
            assertThat(place.getTtsSyncedAt()).isNotNull();
            verify(tourPlaceRepository).save(place);
            verify(textToSpeechModel, never()).call(any(TextToSpeechPrompt.class));
        }
    }

    @Nested
    class 음원_생성 {

        @Test
        void 소개글을_음성으로_변환해_저장하고_장소에_키를_기록한다() {
            TourPlace place = TourPlaceFixture.withOverview("죽녹원", "대나무 숲길이 아름다운 정원입니다.");
            when(tourPlaceRepository.findTtsPending(eq(Locale.KOREAN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(place))).thenReturn(Page.empty());
            when(textToSpeechModel.call(any(TextToSpeechPrompt.class))).thenReturn(response(MP3_BYTES));
            when(audioStorage.storeTts(MP3_BYTES, Locale.KOREAN)).thenReturn("tts/ko/abc.mp3");

            ttsAudioSyncer.sync(Locale.KOREAN);

            ArgumentCaptor<TextToSpeechPrompt> promptCaptor = ArgumentCaptor.forClass(TextToSpeechPrompt.class);
            verify(textToSpeechModel).call(promptCaptor.capture());
            assertThat(promptCaptor.getValue().getInstructions().getText())
                .isEqualTo("대나무 숲길이 아름다운 정원입니다.");
            assertThat(place.getTtsAudioKey()).isEqualTo("tts/ko/abc.mp3");
            assertThat(place.getTtsSyncedAt()).isNotNull();
            verify(tourPlaceRepository).save(place);
        }

        @Test
        void 소개글이_입력_한도를_넘으면_잘라서_변환한다() {
            String overview = "가".repeat(5000);
            TourPlace place = TourPlaceFixture.withOverview("죽녹원", overview);
            when(tourPlaceRepository.findTtsPending(eq(Locale.KOREAN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(place))).thenReturn(Page.empty());
            when(textToSpeechModel.call(any(TextToSpeechPrompt.class))).thenReturn(response(MP3_BYTES));
            when(audioStorage.storeTts(MP3_BYTES, Locale.KOREAN)).thenReturn("tts/ko/abc.mp3");

            ttsAudioSyncer.sync(Locale.KOREAN);

            ArgumentCaptor<TextToSpeechPrompt> promptCaptor = ArgumentCaptor.forClass(TextToSpeechPrompt.class);
            verify(textToSpeechModel).call(promptCaptor.capture());
            assertThat(promptCaptor.getValue().getInstructions().getText()).hasSize(4096);
        }

        @Test
        void 소개글이_비어_있으면_변환하지_않고_음원_없음으로_기록한다() {
            TourPlace place = TourPlaceFixture.withOverview("죽녹원", "   ");
            when(tourPlaceRepository.findTtsPending(eq(Locale.KOREAN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(place))).thenReturn(Page.empty());

            ttsAudioSyncer.sync(Locale.KOREAN);

            verify(textToSpeechModel, never()).call(any(TextToSpeechPrompt.class));
            assertThat(place.getTtsAudioKey()).isNull();
            assertThat(place.getTtsSyncedAt()).isNotNull();
            verify(tourPlaceRepository).save(place);
        }

        @Test
        void 대기_장소가_없으면_아무것도_하지_않는다() {
            when(tourPlaceRepository.findTtsPending(eq(Locale.ENGLISH), any(Pageable.class)))
                .thenReturn(Page.empty());

            ttsAudioSyncer.sync(Locale.ENGLISH);

            verify(textToSpeechModel, never()).call(any(TextToSpeechPrompt.class));
            verify(tourPlaceRepository, never()).save(any());
        }
    }

    @Nested
    class 실패_처리 {

        @Test
        void 한_장소가_실패해도_다음_장소를_계속_진행한다() {
            TourPlace failing = TourPlaceFixture.withOverview("실패 장소", "첫 번째 소개");
            TourPlace succeeding = TourPlaceFixture.withOverview("성공 장소", "두 번째 소개");
            when(tourPlaceRepository.findTtsPending(eq(Locale.KOREAN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(failing, succeeding))).thenReturn(Page.empty());
            when(textToSpeechModel.call(any(TextToSpeechPrompt.class)))
                .thenThrow(new RuntimeException("tts failed"))
                .thenReturn(response(MP3_BYTES));
            when(audioStorage.storeTts(MP3_BYTES, Locale.KOREAN)).thenReturn("tts/ko/abc.mp3");

            ttsAudioSyncer.sync(Locale.KOREAN);

            assertThat(failing.getTtsSyncedAt()).isNull();
            assertThat(succeeding.getTtsAudioKey()).isEqualTo("tts/ko/abc.mp3");
            verify(tourPlaceRepository, never()).save(failing);
            verify(tourPlaceRepository).save(succeeding);
        }

        @Test
        void 업로드가_실패해도_다음_장소를_계속_진행한다() {
            TourPlace failing = TourPlaceFixture.withOverview("실패 장소", "첫 번째 소개");
            TourPlace succeeding = TourPlaceFixture.withOverview("성공 장소", "두 번째 소개");
            when(tourPlaceRepository.findTtsPending(eq(Locale.KOREAN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(failing, succeeding))).thenReturn(Page.empty());
            when(textToSpeechModel.call(any(TextToSpeechPrompt.class))).thenReturn(response(MP3_BYTES));
            when(audioStorage.storeTts(MP3_BYTES, Locale.KOREAN))
                .thenThrow(BusinessException.of(ErrorCode.AUDIO_UPLOAD_FAILED))
                .thenReturn("tts/ko/abc.mp3");

            ttsAudioSyncer.sync(Locale.KOREAN);

            assertThat(failing.getTtsAudioKey()).isNull();
            assertThat(succeeding.getTtsAudioKey()).isEqualTo("tts/ko/abc.mp3");
        }

        @Test
        void 연속으로_실패하면_단계를_중단한다() {
            List<TourPlace> places = List.of(
                TourPlaceFixture.withOverview("장소1", "소개1"),
                TourPlaceFixture.withOverview("장소2", "소개2"),
                TourPlaceFixture.withOverview("장소3", "소개3"),
                TourPlaceFixture.withOverview("장소4", "소개4"));
            when(tourPlaceRepository.findTtsPending(eq(Locale.KOREAN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(places));
            when(textToSpeechModel.call(any(TextToSpeechPrompt.class)))
                .thenThrow(new RuntimeException("rate limited"));

            ttsAudioSyncer.sync(Locale.KOREAN);

            verify(textToSpeechModel, times(3)).call(any(TextToSpeechPrompt.class));
            verify(tourPlaceRepository, times(1)).findTtsPending(eq(Locale.KOREAN), any(Pageable.class));
        }
    }

    private TextToSpeechResponse response(byte[] bytes) {
        return new TextToSpeechResponse(List.of(new Speech(bytes)));
    }
}
