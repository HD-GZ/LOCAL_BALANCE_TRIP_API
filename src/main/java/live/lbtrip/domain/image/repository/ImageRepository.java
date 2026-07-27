package live.lbtrip.domain.image.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.global.storage.ImageDirectory;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Image> findByIdAndUploaderIdAndDirectory(
        Long id,
        Long uploaderId,
        ImageDirectory directory
    );
}
