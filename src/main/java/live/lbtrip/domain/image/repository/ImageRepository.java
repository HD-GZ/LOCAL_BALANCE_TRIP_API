package live.lbtrip.domain.image.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.global.storage.enums.ImageDirectory;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByIdAndUploaderIdAndDirectory(
        Long id,
        Long uploaderId,
        ImageDirectory directory
    );
}
