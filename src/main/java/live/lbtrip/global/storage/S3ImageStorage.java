package live.lbtrip.global.storage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.global.config.StorageProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
public class S3ImageStorage implements ImageStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final DateTimeFormatter KEY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final S3Client s3Client;
    private final StorageProperties properties;

    public S3ImageStorage(S3Client s3Client, StorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public String store(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE);
        }
        String extension = extractExtension(file);
        String key = "%s/%s/%s.%s".formatted(
            directory, LocalDate.now().format(KEY_DATE_FORMAT), UUID.randomUUID(), extension);

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(properties.s3().bucket())
            .key(key)
            .contentType(file.getContentType())
            .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException | RuntimeException e) {
            log.error("S3 이미지 업로드 실패: key={}", key, e);
            throw BusinessException.of(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
        return key;
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
            .bucket(properties.s3().bucket())
            .key(key)
            .build();
        try {
            s3Client.deleteObject(request);
        } catch (RuntimeException e) {
            log.error("S3 이미지 삭제 실패: key={}", key, e);
        }
    }

    @Override
    public String publicUrl(String key) {
        return "%s/%s".formatted(properties.cdnBaseUrl(), key);
    }

    private String extractExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw BusinessException.of(ErrorCode.INVALID_IMAGE_TYPE);
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw BusinessException.of(ErrorCode.INVALID_IMAGE_TYPE);
        }
        return extension;
    }
}
