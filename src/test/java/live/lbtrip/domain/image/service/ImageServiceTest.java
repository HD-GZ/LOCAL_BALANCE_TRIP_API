package live.lbtrip.domain.image.service;

import static live.lbtrip.global.storage.ImageDirectory.RECEIPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import live.lbtrip.domain.image.model.ImageStatus;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.ImageStorage;
import live.lbtrip.global.storage.ValidatedImage;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final Long IMAGE_ID = 1L;
    private static final Long UPLOADER_ID = 2L;
    private static final String IMAGE_KEY = "receipts/test.jpg";
    private static final String IMAGE_URL = "https://images.example.com/" + IMAGE_KEY;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private User uploader;

    @InjectMocks
    private ImageService imageService;

    @Nested
    class 등록 {

        @Test
        void 업로더_소유의_임시_이미지를_저장한다() {
            ValidatedImage validatedImage = ValidatedImage.of(
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
                "jpg",
                MediaType.IMAGE_JPEG
            );
            when(userFinder.findById(UPLOADER_ID)).thenReturn(uploader);
            when(imageStorage.store(validatedImage, RECEIPT)).thenReturn(IMAGE_KEY);
            when(imageRepository.save(any(Image.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Image result = imageService.register(UPLOADER_ID, RECEIPT, validatedImage);

            assertThat(result.getUploader()).isSameAs(uploader);
            assertThat(result.getDirectory()).isEqualTo(RECEIPT);
            assertThat(result.getStorageKey()).isEqualTo(IMAGE_KEY);
            assertThat(result.getContentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
            assertThat(result.getFileSize()).isEqualTo(validatedImage.size());
            assertThat(result.getStatus()).isEqualTo(ImageStatus.TEMPORARY);
            verify(imageRepository).save(result);
        }
    }

    @Nested
    class 사용 {

        @Test
        void 업로더_소유의_이미지를_도메인에_연결한다() {
            Image image = Image.create(
                uploader,
                RECEIPT,
                "receipts/test.jpg",
                "image/jpeg",
                100
            );
            when(imageRepository.findByIdAndUploaderIdAndDirectory(
                IMAGE_ID,
                UPLOADER_ID,
                RECEIPT
            )).thenReturn(Optional.of(image));

            Image result = imageService.claim(IMAGE_ID, UPLOADER_ID, RECEIPT);

            assertThat(result.getStatus()).isEqualTo(ImageStatus.ATTACHED);
        }

        @Test
        void 다른_업로더의_이미지는_찾을_수_없다() {
            when(imageRepository.findByIdAndUploaderIdAndDirectory(
                IMAGE_ID,
                UPLOADER_ID,
                RECEIPT
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageService.claim(IMAGE_ID, UPLOADER_ID, RECEIPT))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_NOT_FOUND);
        }
    }

    @Nested
    class 저장소_접근 {

        @Test
        void 이미지의_공개_URL을_조회한다() {
            Image image = image();
            when(imageStorage.publicUrl(IMAGE_KEY)).thenReturn(IMAGE_URL);

            String result = imageService.getPublicUrl(image);

            assertThat(result).isEqualTo(IMAGE_URL);
        }

        @Test
        void 저장소에서_이미지를_삭제한다() {
            Image image = image();

            imageService.delete(image);

            verify(imageStorage).delete(IMAGE_KEY);
        }
    }

    private Image image() {
        return Image.create(
            uploader,
            RECEIPT,
            IMAGE_KEY,
            MediaType.IMAGE_JPEG_VALUE,
            100
        );
    }
}
