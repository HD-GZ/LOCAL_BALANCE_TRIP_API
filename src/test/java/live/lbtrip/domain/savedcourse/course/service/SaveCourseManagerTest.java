package live.lbtrip.domain.savedcourse.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.savedcourse.course.repository.SavedCourseRepository;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class SaveCourseManagerTest {

    @Mock
    private SavedCourseRepository savedCourseRepository;

    @InjectMocks
    private SaveCourseManager saveCourseManager;

    @Test
    void 추천_코스의_장소_스냅샷을_추천_이유까지_복사해_저장한다() {
        GeneratedCourse generatedCourse = RecommendationFixture.region().getCourses().getFirst();

        saveCourseManager.add(generatedCourse, UserFixture.user());

        ArgumentCaptor<SavedCourse> captor = ArgumentCaptor.forClass(SavedCourse.class);
        verify(savedCourseRepository).save(captor.capture());
        assertThat(captor.getValue().getPlaces())
            .extracting(SavedCoursePlace::getName, SavedCoursePlace::getReason)
            .containsExactly(
                tuple("죽녹원", RecommendationFixture.PLACE_REASON),
                tuple("관방제림", null));
    }
}
