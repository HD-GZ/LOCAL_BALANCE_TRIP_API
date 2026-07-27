package live.lbtrip.domain.image.service;

import static live.lbtrip.global.storage.enums.ImageDirectory.RECEIPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.domain.image.model.enums.ImageStatus;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.model.vo.ImageRegistration;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.ImageFileValidator;
import live.lbtrip.global.storage.ImageStorage;
import live.lbtrip.global.storage.vo.ValidatedImage;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final Long IMAGE_ID = 1L;
    private static final Long UPLOADER_ID = 2L;
    private static final String IMAGE_KEY = "receipts/test.jpg";
    private static final String IMAGE_URL = "https://images.example.com/" + IMAGE_KEY;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageManager imageManager;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ImageFileValidator imageFileValidator;

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private MultipartFile imageFile;

    @Mock
    private Image registeredImage;

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
            when(imageFileValidator.validate(imageFile)).thenReturn(validatedImage);
            when(imageStorage.store(validatedImage, RECEIPT)).thenReturn(IMAGE_KEY);
            when(imageManager.add(uploader, RECEIPT, IMAGE_KEY, validatedImage))
                .thenReturn(registeredImage);

            ImageRegistration result = imageService.register(UPLOADER_ID, RECEIPT, imageFile);

            assertThat(result.image()).isSameAs(registeredImage);
            assertThat(result.validatedImage()).isSameAs(validatedImage);
            verify(imageManager).add(uploader, RECEIPT, IMAGE_KEY, validatedImage);
        }

        @Test
        void 유효하지_않은_이미지는_저장소에_업로드하지_않는다() {
            when(userFinder.findById(UPLOADER_ID)).thenReturn(uploader);
            when(imageFileValidator.validate(imageFile))
                .thenThrow(BusinessException.of(ErrorCode.INVALID_IMAGE_TYPE));

            assertThatThrownBy(() -> imageService.register(UPLOADER_ID, RECEIPT, imageFile))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_IMAGE_TYPE);

            verify(imageStorage, never()).store(any(), any());
            verify(imageManager, never()).add(any(), any(), any(), any());
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
