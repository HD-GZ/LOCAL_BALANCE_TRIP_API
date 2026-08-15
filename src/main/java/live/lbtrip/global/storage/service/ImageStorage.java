package live.lbtrip.global.storage.service;

import live.lbtrip.global.storage.enums.ImageDirectory;
import live.lbtrip.global.storage.vo.PresignedUrl;
import live.lbtrip.global.storage.vo.ValidatedImage;

public interface ImageStorage {

    String store(ValidatedImage image, ImageDirectory directory);

    void delete(String key);

    String publicUrl(String key);

    String presignedViewUrl(String key);

    PresignedUrl presignedDownloadUrl(String key, String downloadFilename);
}
