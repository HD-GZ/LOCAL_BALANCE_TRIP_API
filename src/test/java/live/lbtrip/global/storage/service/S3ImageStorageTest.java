package live.lbtrip.global.storage.service;

import static live.lbtrip.global.storage.enums.ImageDirectory.RECEIPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;

import live.lbtrip.global.config.StorageProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.vo.ValidatedImage;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3ImageStorageTest {

    @Mock
    private S3Client s3Client;

    private S3ImageStorage imageStorage;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties(
            new StorageProperties.S3("bucket", "ap-northeast-2", "access-key", "secret-key"),
            "https://images.example.com",
            DataSize.ofMegabytes(10)
        );
        imageStorage = new S3ImageStorage(s3Client, properties);
    }

    @Nested
    class 저장 {

        @Test
        void 검증된_MIME_타입으로_이미지를_저장한다() {
            ValidatedImage image = ValidatedImage.of(
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
                "jpg",
                MediaType.IMAGE_JPEG
            );
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

            String key = imageStorage.store(image, RECEIPT);

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
            assertThat(key).startsWith("receipts/").endsWith(".jpg");
            assertThat(requestCaptor.getValue().contentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        }

        @Test
        void S3_업로드에_실패하면_비즈니스_예외를_던진다() {
            ValidatedImage image = ValidatedImage.of(
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
                "jpg",
                MediaType.IMAGE_JPEG
            );
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("upload failed"));

            assertThatThrownBy(() -> imageStorage.store(image, RECEIPT))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}
