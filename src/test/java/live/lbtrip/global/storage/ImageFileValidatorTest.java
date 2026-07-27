package live.lbtrip.global.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import live.lbtrip.global.config.StorageProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class ImageFileValidatorTest {

    private static final long MAX_IMAGE_SIZE = 1024;

    private final ImageFileValidator imageFileValidator = new ImageFileValidator(
        new StorageProperties(null, null, DataSize.ofBytes(MAX_IMAGE_SIZE))
    );

    @Nested
    class 검증_성공 {

        @Test
        void JPEG_시그니처와_확장자를_검증한다() {
            MockMultipartFile file = file("receipt.jpg", "text/html", bytes(0xFF, 0xD8, 0xFF));

            ValidatedImage result = imageFileValidator.validate(file);

            assertThat(result.extension()).isEqualTo("jpg");
            assertThat(result.mediaType()).isEqualTo(MediaType.IMAGE_JPEG);
        }

        @Test
        void PNG_시그니처와_확장자를_검증한다() {
            MockMultipartFile file = file(
                "receipt.png",
                MediaType.IMAGE_PNG_VALUE,
                bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
            );

            ValidatedImage result = imageFileValidator.validate(file);

            assertThat(result.mediaType()).isEqualTo(MediaType.IMAGE_PNG);
        }

        @Test
        void WEBP_시그니처와_확장자를_검증한다() {
            MockMultipartFile file = file(
                "receipt.webp",
                "image/webp",
                bytes(0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50)
            );

            ValidatedImage result = imageFileValidator.validate(file);

            assertThat(result.mediaType().toString()).isEqualTo("image/webp");
        }
    }

    @Nested
    class 검증_실패 {

        @Test
        void 파일_내용이_이미지가_아니면_예외를_던진다() {
            MockMultipartFile file = file("receipt.jpg", MediaType.IMAGE_JPEG_VALUE, "html".getBytes());

            assertErrorCode(file, ErrorCode.INVALID_IMAGE_TYPE);
        }

        @Test
        void 시그니처와_확장자가_다르면_예외를_던진다() {
            MockMultipartFile file = file(
                "receipt.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
            );

            assertErrorCode(file, ErrorCode.INVALID_IMAGE_TYPE);
        }

        @Test
        void 이미지가_10MB를_초과하면_예외를_던진다() {
            byte[] content = new byte[(int) MAX_IMAGE_SIZE + 1];
            content[0] = (byte) 0xFF;
            content[1] = (byte) 0xD8;
            content[2] = (byte) 0xFF;
            MockMultipartFile file = file("receipt.jpg", MediaType.IMAGE_JPEG_VALUE, content);

            assertErrorCode(file, ErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }

    private MockMultipartFile file(String filename, String contentType, byte[] content) {
        return new MockMultipartFile("image", filename, contentType, content);
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            bytes[index] = (byte) values[index];
        }
        return bytes;
    }

    private void assertErrorCode(MockMultipartFile file, ErrorCode errorCode) {
        assertThatThrownBy(() -> imageFileValidator.validate(file))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(errorCode);
    }
}
