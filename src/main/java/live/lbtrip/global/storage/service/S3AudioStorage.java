package live.lbtrip.global.storage.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;

import live.lbtrip.global.config.StorageProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3AudioStorage implements AudioStorage {

    private static final String TTS_DIRECTORY = "tts";
    private static final String MP3_CONTENT_TYPE = "audio/mpeg";

    private final S3Client s3Client;
    private final StorageProperties properties;

    @Override
    public String storeTts(byte[] audio, Locale locale) {
        String key = "%s/%s/%s.mp3".formatted(TTS_DIRECTORY, locale.getLanguage(), UUID.randomUUID());

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(properties.s3().bucket())
            .key(key)
            .contentType(MP3_CONTENT_TYPE)
            .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(audio));
        } catch (RuntimeException e) {
            log.error("S3 오디오 업로드 실패: key={}", key, e);
            throw BusinessException.of(ErrorCode.AUDIO_UPLOAD_FAILED);
        }
        return key;
    }

    @Override
    public String publicUrl(String key) {
        return "%s/%s".formatted(properties.cdnBaseUrl(), key);
    }
}
