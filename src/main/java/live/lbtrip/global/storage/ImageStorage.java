package live.lbtrip.global.storage;

public interface ImageStorage {

    String store(ValidatedImage image, ImageDirectory directory);

    void delete(String key);

    String publicUrl(String key);
}
