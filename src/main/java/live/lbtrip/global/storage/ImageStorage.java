package live.lbtrip.global.storage;

public interface ImageStorage {

    String store(ValidatedImage image, String directory);

    void delete(String key);

    String publicUrl(String key);
}
