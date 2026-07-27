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

import live.lbtrip.domain.image.model.ImageStatus;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final Long IMAGE_ID = 1L;
    private static final Long UPLOADER_ID = 2L;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private User uploader;

    @InjectMocks
    private ImageService imageService;

    @Nested
    class 등록 {

        @Test
        void 업로더_소유의_임시_이미지를_저장한다() {
            when(userFinder.findById(UPLOADER_ID)).thenReturn(uploader);
            when(imageRepository.save(any(Image.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Image result = imageService.register(
                UPLOADER_ID,
                RECEIPT,
                "receipts/test.jpg",
                "image/jpeg",
                100
            );

            assertThat(result.getUploader()).isSameAs(uploader);
            assertThat(result.getDirectory()).isEqualTo(RECEIPT);
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
}
