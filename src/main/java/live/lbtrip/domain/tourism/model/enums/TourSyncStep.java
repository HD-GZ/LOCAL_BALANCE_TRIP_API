package live.lbtrip.domain.tourism.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TourSyncStep {

    REGIONS("지역별 통계·장소·오디오 테마 적재"),
    PLACE_THEMES("장소-오디오 테마 매칭"),
    OVERVIEWS("장소 상세 설명 보강"),
    AUDIO_URLS("오디오 테마 음원 URL 보강"),
    VISITOR_STATS("지역별 방문자수 적재");

    private final String description;
}
