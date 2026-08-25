package live.lbtrip.global.storage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;

import live.lbtrip.global.config.StorageProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3AudioStorageTest {

    private static final byte[] MP3_BYTES = {0x49, 0x44, 0x33};

    @Mock
    private S3Client s3Client;

    private S3AudioStorage audioStorage;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties(
            new StorageProperties.S3("bucket", "ap-northeast-2", "access-key", "secret-key"),
            "https://images.example.com",
            DataSize.ofMegabytes(10),
            Duration.ofMinutes(10)
        );
        audioStorage = new S3AudioStorage(s3Client, properties);
    }

    @Nested
    class 저장 {

        @Test
        void 로케일별_tts_경로에_mp3로_저장한다() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

            String key = audioStorage.storeTts(MP3_BYTES, Locale.ENGLISH);

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
            assertThat(key).startsWith("tts/en/").endsWith(".mp3");
            assertThat(requestCaptor.getValue().bucket()).isEqualTo("bucket");
            assertThat(requestCaptor.getValue().key()).isEqualTo(key);
            assertThat(requestCaptor.getValue().contentType()).isEqualTo("audio/mpeg");
        }

        @Test
        void S3_업로드에_실패하면_비즈니스_예외를_던진다() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("upload failed"));

            assertThatThrownBy(() -> audioStorage.storeTts(MP3_BYTES, Locale.KOREAN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUDIO_UPLOAD_FAILED);
        }
    }

    @Nested
    class 공개_URL {

        @Test
        void CDN_기본_URL에_키를_붙여_반환한다() {
            assertThat(audioStorage.publicUrl("tts/ko/abc.mp3"))
                .isEqualTo("https://images.example.com/tts/ko/abc.mp3");
        }
    }
}
