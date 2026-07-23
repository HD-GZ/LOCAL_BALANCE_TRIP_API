package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.admin.incentive.model.Incentive;
import live.lbtrip.domain.admin.incentive.repository.IncentiveRepository;
import live.lbtrip.domain.recommendation.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.recommendation.model.entity.SavedCourse;
import live.lbtrip.domain.recommendation.repository.SavedCourseRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.web.PageQueryRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedCourseService {

    private final SavedCourseRepository savedCourseRepository;
    private final IncentiveRepository incentiveRepository;

    public SavedCourseListResponse getSavedCourses(Long userId, PageQueryRequest pageQuery) {
        Page<SavedCourse> savedCourses = savedCourseRepository.findAllByUserIdOrderByIdDesc(
            userId, pageQuery.toPageRequest());

        return SavedCourseListResponse.from(savedCourses);
    }

    public SavedCourseDetailResponse getSavedCourseDetail(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseRepository.findByIdAndUserId(savedCourseId, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.SAVED_COURSE_NOT_FOUND));

        return SavedCourseDetailResponse.of(savedCourse, findApplicableIncentives(savedCourse));
    }

    private List<Incentive> findApplicableIncentives(SavedCourse savedCourse) {
        if (savedCourse.getLdongRegnCd() == null || savedCourse.getLdongSignguCd() == null) {
            return List.of();
        }
        return incentiveRepository.findAllByRegion(savedCourse.getLdongRegnCd(), savedCourse.getLdongSignguCd());
    }
}
