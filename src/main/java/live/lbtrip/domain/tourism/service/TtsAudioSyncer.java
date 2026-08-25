package live.lbtrip.domain.tourism.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.storage.service.AudioStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TtsAudioSyncer {

    private static final int MAX_INPUT_LENGTH = 4096;
    private static final int PAGE_SIZE = 50;
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    private final TourPlaceRepository tourPlaceRepository;
    private final TextToSpeechModel textToSpeechModel;
    private final AudioStorage audioStorage;

    public void sync(Locale locale) {
        Pageable firstPage = PageRequest.of(0, PAGE_SIZE);
        int successCount = 0;
        int failureCount = 0;
        int consecutiveFailures = 0;
        Page<TourPlace> pending = tourPlaceRepository.findTtsPending(locale, firstPage);
        while (pending.hasContent()) {
            int successBefore = successCount;
            for (TourPlace place : pending.getContent()) {
                if (generate(place, locale)) {
                    successCount++;
                    consecutiveFailures = 0;
                    continue;
                }
                failureCount++;
                if (++consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    log.warn("TTS 음원 생성 중단 - 연속 실패: locale={}, success={}, failure={}",
                        locale, successCount, failureCount);
                    return;
                }
            }
            if (successCount == successBefore) {
                break;
            }
            pending = tourPlaceRepository.findTtsPending(locale, firstPage);
        }
        log.info("TTS 음원 생성 완료: locale={}, success={}, failure={}", locale, successCount, failureCount);
    }

    private boolean generate(TourPlace place, Locale locale) {
        try {
            String input = speechInput(place.getOverview());
            if (input.isEmpty()) {
                place.markTtsAudioUnavailable(LocalDateTime.now());
            } else {
                byte[] audio = textToSpeechModel.call(new TextToSpeechPrompt(input)).getResult().getOutput();
                String key = audioStorage.storeTts(audio, locale);
                place.updateTtsAudio(key, LocalDateTime.now());
            }
            tourPlaceRepository.save(place);
            return true;
        } catch (Exception e) {
            log.warn("TTS 음원 생성 실패 - 다음 장소 진행: locale={}, contentId={}", locale, place.getContentId(), e);
            return false;
        }
    }

    private String speechInput(String overview) {
        String trimmed = overview == null ? "" : overview.trim();
        return trimmed.length() <= MAX_INPUT_LENGTH ? trimmed : trimmed.substring(0, MAX_INPUT_LENGTH);
    }
}
