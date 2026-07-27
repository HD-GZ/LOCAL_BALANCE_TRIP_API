package live.lbtrip.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

    String store(MultipartFile file, String directory);

    void delete(String key);

    String publicUrl(String key);
}
