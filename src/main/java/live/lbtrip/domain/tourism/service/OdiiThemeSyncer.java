package live.lbtrip.domain.tourism.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.client.OdiiClient;
import live.lbtrip.domain.tourism.client.dto.OdiiThemeItem;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.vo.Centroid;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OdiiThemeSyncer {

    private final OdiiClient odiiClient;
    private final OdiiThemeRepository odiiThemeRepository;

    public void sync(List<TourPlaceItem> places) {
        Centroid.of(places, TourPlaceItem::longitude, TourPlaceItem::latitude)
            .ifPresent(this::upsertThemesNear);
    }

    public void syncAudioUrls() {
        List<OdiiTheme> pending = odiiThemeRepository.findAllByAudioSyncedAtIsNull();
        int successCount = 0;
        for (OdiiTheme theme : pending) {
            try {
                String audioUrl = odiiClient.fetchFirstAudioUrl(theme.getTid(), theme.getTlid());
                theme.updateAudio(audioUrl, LocalDateTime.now());
                odiiThemeRepository.save(theme);
                successCount++;
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.TOUR_API_QUOTA_EXCEEDED) {
                    throw e;
                }
                log.warn("Odii 오디오 적재 중단 - 일일 한도 초과: success={}/{}", successCount, pending.size());
                return;
            }
        }
        log.info("Odii 오디오 적재 완료: success={}/{}", successCount, pending.size());
    }

    private void upsertThemesNear(Centroid centroid) {
        for (OdiiThemeItem item : odiiClient.fetchThemesNear(centroid.longitude(), centroid.latitude())) {
            upsert(item);
        }
    }

    private void upsert(OdiiThemeItem item) {
        odiiThemeRepository.findByTidAndTlid(item.tid(), item.tlid())
            .ifPresentOrElse(
                odiiTheme -> {
                    odiiTheme.update(item.title(), item.longitude(), item.latitude());
                    odiiThemeRepository.save(odiiTheme);
                },
                () -> odiiThemeRepository.save(OdiiTheme.create(
                    item.tid(),
                    item.tlid(),
                    item.title(),
                    item.longitude(),
                    item.latitude()
                ))
            );
    }
}
