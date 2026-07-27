package live.lbtrip.domain.image.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.ImageDirectory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserFinder userFinder;

    @Transactional
    public Image register(
        Long uploaderId,
        ImageDirectory directory,
        String storageKey,
        String contentType,
        long fileSize
    ) {
        User uploader = userFinder.findById(uploaderId);
        Image image = Image.create(uploader, directory, storageKey, contentType, fileSize);
        return imageRepository.save(image);
    }

    @Transactional
    public Image claim(Long imageId, Long uploaderId, ImageDirectory directory) {
        Image image = imageRepository.findByIdAndUploaderIdAndDirectory(
                imageId,
                uploaderId,
                directory
            )
            .orElseThrow(() -> BusinessException.of(ErrorCode.IMAGE_NOT_FOUND));
        image.attach();
        return image;
    }
}
