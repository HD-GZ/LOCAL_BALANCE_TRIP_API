package live.lbtrip.global.storage.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

import live.lbtrip.global.config.StorageProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.enums.ImageDirectory;
import live.lbtrip.global.storage.vo.ValidatedImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageStorage implements ImageStorage {

    private static final DateTimeFormatter KEY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final S3Client s3Client;
    private final StorageProperties properties;

    @Override
    public String store(ValidatedImage image, ImageDirectory directory) {
        String key = "%s/%s/%s.%s".formatted(
            directory.getPath(),
            LocalDate.now().format(KEY_DATE_FORMAT),
            UUID.randomUUID(),
            image.extension()
        );

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(properties.s3().bucket())
            .key(key)
            .contentType(image.mediaType().toString())
            .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(image.bytes()));
        } catch (RuntimeException e) {
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

}
