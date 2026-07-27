package live.lbtrip.domain.image.model;

import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.global.storage.ValidatedImage;

public record ImageRegistration(
    Image image,
    ValidatedImage validatedImage
) {

    public static ImageRegistration of(Image image, ValidatedImage validatedImage) {
        return new ImageRegistration(image, validatedImage);
    }
}
