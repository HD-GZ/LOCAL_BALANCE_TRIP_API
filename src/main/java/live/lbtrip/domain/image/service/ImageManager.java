package live.lbtrip.domain.image.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.storage.ImageDirectory;
import live.lbtrip.global.storage.ValidatedImage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageManager {

    private final ImageRepository imageRepository;

    public Image add(
        User uploader,
        ImageDirectory directory,
        String storageKey,
        ValidatedImage validatedImage
    ) {
        Image image = Image.create(
            uploader,
            directory,
            storageKey,
            validatedImage.mediaType().toString(),
            validatedImage.size()
        );
        return imageRepository.save(image);
    }
}
