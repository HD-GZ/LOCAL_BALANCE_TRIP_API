package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.RegionComposition;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.domain.tourism.service.TourPlaceFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegionCompositionAssembler {

    private final TourPlaceFinder tourPlaceFinder;
    private final CourseComposer courseComposer;

    public List<RegionComposition> assemble(Propensity propensity, List<RegionMetrics> regions) {
        List<RegionComposition> compositions = new ArrayList<>();
        for (RegionMetrics region : regions) {
            List<TourPlace> places = tourPlaceFinder.findAllByRegionCandidateId(region.regionCandidateId());
            try {
                CourseComposition composition = courseComposer.compose(propensity, region.regionName(), places);
                compositions.add(RegionComposition.of(region, composition));
            } catch (BusinessException e) {
                log.warn("코스 구성에 실패한 지역을 건너뜁니다: region={}", region.regionName());
            }
        }
        if (compositions.isEmpty()) {
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        return compositions;
    }
}
