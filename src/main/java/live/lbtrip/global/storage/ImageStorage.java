package live.lbtrip.global.storage;

import live.lbtrip.global.storage.enums.ImageDirectory;
import live.lbtrip.global.storage.vo.ValidatedImage;

public interface ImageStorage {

    String store(ValidatedImage image, ImageDirectory directory);

    void delete(String key);

    String publicUrl(String key);
}
