package live.lbtrip.domain.tourism.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.model.RegionGreenMetrics;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.region.repository.RegionGreenMetricsRepository;
import live.lbtrip.domain.tourism.client.DurunubiClient;
import live.lbtrip.domain.tourism.client.dto.DurunubiCourseItem;
import live.lbtrip.domain.tourism.model.entity.TrailCourse;
import live.lbtrip.domain.tourism.repository.TrailCourseRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrailCourseSyncer {

    private final RegionCandidateRepository regionCandidateRepository;
    private final DurunubiClient durunubiClient;
    private final TrailCourseRepository trailCourseRepository;
    private final RegionGreenMetricsRepository regionGreenMetricsRepository;

    public void sync() {
        TrailRegionMatcher matcher = new TrailRegionMatcher(regionCandidateRepository.findAll());
        try {
            int upserted = syncPages(matcher);
            log.info("두루누비 코스 적재 완료: upserted={}", upserted);
        } catch (BusinessException e) {
            if (e.getErrorCode() != ErrorCode.TOUR_API_QUOTA_EXCEEDED) {
                throw e;
            }
            log.warn("두루누비 코스 적재 중단 - 일일 한도 초과");
        } finally {
            updateGpxAdjacent();
        }
    }

    private int syncPages(TrailRegionMatcher matcher) {
        int upserted = 0;
        int pageNo = 1;
        while (true) {
            List<DurunubiCourseItem> items = durunubiClient.fetchCourses(pageNo);
            for (DurunubiCourseItem item : items) {
                upsert(item, matcher.match(item.sigun()).orElse(null));
                upserted++;
            }
            if (items.size() < DurunubiClient.PAGE_SIZE) {
                return upserted;
            }
            pageNo++;
        }
    }

    private void upsert(DurunubiCourseItem item, RegionCandidate candidate) {
        trailCourseRepository.findByCrsIdx(item.crsIdx())
            .ifPresentOrElse(
                existing -> {
                    existing.update(item, candidate);
                    trailCourseRepository.save(existing);
                },
                () -> trailCourseRepository.save(TrailCourse.create(item, candidate)));
    }

    private void updateGpxAdjacent() {
        Set<Long> regionIdsWithCourses = new HashSet<>(trailCourseRepository.findRegionCandidateIdsWithCourses());
        List<RegionGreenMetrics> metrics = regionGreenMetricsRepository.findAllWithRegionCandidate();
        for (RegionGreenMetrics metric : metrics) {
            metric.updateGpxAdjacent(regionIdsWithCourses.contains(metric.getRegionCandidate().getId()));
        }
        regionGreenMetricsRepository.saveAll(metrics);
        log.info("gpx 인접 여부 갱신 완료: adjacentRegions={}, metricsCount={}",
            regionIdsWithCourses.size(), metrics.size());
    }
}
