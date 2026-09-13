package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.PlacePlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.PlannedCourse;
import live.lbtrip.domain.recommendation.model.vo.RoutedPlace;
import live.lbtrip.domain.recommendation.model.vo.WalkableCluster;
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
public class RegionPlanAssembler {

    private final TourPlaceFinder tourPlaceFinder;
    private final WalkableClusterBuilder walkableClusterBuilder;
    private final CourseComposer courseComposer;
    private final CourseRoutePlanner courseRoutePlanner;

    public List<RegionPlan> assemble(Propensity propensity, List<RegionMetrics> regions, Locale locale) {
        List<RegionPlan> plans = new ArrayList<>();
        for (RegionMetrics region : regions) {
            List<TourPlace> places = tourPlaceFinder.findAllByRegionCandidateId(region.regionCandidateId(), locale);
            List<WalkableCluster> clusters = walkableClusterBuilder.build(places);
            if (clusters.isEmpty()) {
                log.warn("도보권 클러스터가 없는 지역을 건너뜁니다: region={}", region.regionName());
                continue;
            }
            try {
                String regionName = region.regionNameFor(locale);
                CourseComposition composition = courseComposer.compose(propensity, regionName, clusters, locale);
                plans.add(toRegionPlan(region, regionName, composition, places));
            } catch (BusinessException e) {
                log.warn("코스 구성에 실패한 지역을 건너뜁니다: region={}", region.regionName());
            }
        }
        if (plans.isEmpty()) {
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        return plans;
    }

    private RegionPlan toRegionPlan(
        RegionMetrics region, String regionName, CourseComposition composition, List<TourPlace> places
    ) {
        Map<String, TourPlace> placesById = new HashMap<>();
        for (TourPlace place : places) {
            placesById.put(place.getContentId(), place);
        }

        List<PlannedCourse> courses = new ArrayList<>();
        for (CoursePlan coursePlan : composition.courses()) {
            List<TourPlace> selected = new ArrayList<>();
            Map<String, String> reasonsByContentId = new HashMap<>();
            for (PlacePlan placePlan : coursePlan.places()) {
                selected.add(placesById.get(placePlan.contentId()));
                reasonsByContentId.put(placePlan.contentId(), placePlan.reason());
            }
            List<RoutedPlace> routed = new ArrayList<>();
            for (RoutedPlace routedPlace : courseRoutePlanner.plan(selected)) {
                routed.add(RoutedPlace.of(
                    routedPlace.place(),
                    routedPlace.walkMinutes(),
                    reasonsByContentId.get(routedPlace.place().getContentId())));
            }
            courses.add(PlannedCourse.of(coursePlan.name(), coursePlan.reason(), routed));
        }
        return RegionPlan.of(region, regionName, composition.regionReason(), courses);
    }
}
