package live.lbtrip.domain.image.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.domain.image.model.ImageRegistration;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.ImageDirectory;
import live.lbtrip.global.storage.ImageFileValidator;
import live.lbtrip.global.storage.ImageStorage;
import live.lbtrip.global.storage.ValidatedImage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageManager imageManager;
    private final UserFinder userFinder;
    private final ImageFileValidator imageFileValidator;
    private final ImageStorage imageStorage;

    @Transactional
    public ImageRegistration register(
        Long uploaderId,
        ImageDirectory directory,
        MultipartFile imageFile
    ) {
        User uploader = userFinder.findById(uploaderId);
        ValidatedImage validatedImage = imageFileValidator.validate(imageFile);
        String storageKey = imageStorage.store(validatedImage, directory);
        Image registeredImage = imageManager.add(
            uploader,
            directory,
            storageKey,
            validatedImage
        );
        return ImageRegistration.of(registeredImage, validatedImage);
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

    public String getPublicUrl(Image image) {
        return imageStorage.publicUrl(image.getStorageKey());
    }

    public void delete(Image image) {
        imageStorage.delete(image.getStorageKey());
    }
}
