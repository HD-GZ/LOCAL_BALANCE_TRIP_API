package live.lbtrip.domain.savedcourse.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.savedcourse.model.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;

public interface SavedCourseRepository extends JpaRepository<SavedCourse, Long> {

    boolean existsByUserIdAndSourceCourseId(Long userId, Long sourceCourseId);

    Page<SavedCourse> findAllByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    Page<SavedCourse> findAllByUserIdAndStatusOrderByIdDesc(Long userId, SavedCourseStatus status, Pageable pageable);

    Optional<SavedCourse> findByIdAndUserId(Long id, Long userId);
}
