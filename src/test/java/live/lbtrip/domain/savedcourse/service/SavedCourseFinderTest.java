package live.lbtrip.domain.savedcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.repository.SavedCourseRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class SavedCourseFinderTest {

    private static final Long USER_ID = 1L;
    private static final Long SAVED_COURSE_ID = 2L;

    @Mock
    private SavedCourseRepository savedCourseRepository;

    @Mock
    private SavedCourse savedCourse;

    @InjectMocks
    private SavedCourseFinder savedCourseFinder;

    @Nested
    class 목록_조회 {

        @Test
        void 사용자의_저장_코스_목록을_조회한다() {
            PageRequest pageable = PageRequest.of(0, 10);
            Page<SavedCourse> page = new PageImpl<>(List.of(savedCourse), pageable, 1);
            when(savedCourseRepository.findAllByUserIdOrderByIdDesc(USER_ID, pageable))
                .thenReturn(page);

            Page<SavedCourse> result = savedCourseFinder.findAllByUserId(USER_ID, null, pageable);

            assertThat(result).isSameAs(page);
        }

        @Test
        void 여행_상태를_지정하면_해당_상태의_저장_코스만_조회한다() {
            PageRequest pageable = PageRequest.of(0, 10);
            Page<SavedCourse> page = new PageImpl<>(List.of(savedCourse), pageable, 1);
            when(savedCourseRepository.findAllByUserIdAndStatusOrderByIdDesc(
                USER_ID, SavedCourseStatus.TRAVELING, pageable))
                .thenReturn(page);

            Page<SavedCourse> result = savedCourseFinder.findAllByUserId(
                USER_ID, SavedCourseStatus.TRAVELING, pageable);

            assertThat(result).isSameAs(page);
        }
    }

    @Nested
    class 상세_조회 {

        @Test
        void 사용자의_저장_코스를_조회한다() {
            when(savedCourseRepository.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(Optional.of(savedCourse));

            SavedCourse result = savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID);

            assertThat(result).isSameAs(savedCourse);
        }

        @Test
        void 저장_코스가_없으면_예외를_던진다() {
            when(savedCourseRepository.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SAVED_COURSE_NOT_FOUND);
        }
    }
}
