package live.lbtrip.domain.savedcourse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import live.lbtrip.domain.savedcourse.model.ImagePurpose;
import live.lbtrip.domain.savedcourse.model.entity.StoredImage;

public interface StoredImageRepository extends JpaRepository<StoredImage, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StoredImage> findByIdAndSavedCourseIdAndPurpose(
        Long id,
        Long savedCourseId,
        ImagePurpose purpose
    );
}
