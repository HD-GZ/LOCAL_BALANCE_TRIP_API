package live.lbtrip.domain.image.service;

import static live.lbtrip.global.storage.ImageDirectory.RECEIPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import live.lbtrip.global.storage.ValidatedImage;

@ExtendWith(MockitoExtension.class)
class ImageManagerTest {

    private static final String IMAGE_KEY = "receipts/test.jpg";

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private User uploader;

    @InjectMocks
    private ImageManager imageManager;

    @Test
    void 업로더_소유의_임시_이미지를_추가한다() {
        ValidatedImage validatedImage = ValidatedImage.of(
            new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "jpg",
            MediaType.IMAGE_JPEG
        );
        when(imageRepository.save(any(Image.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Image result = imageManager.add(uploader, RECEIPT, IMAGE_KEY, validatedImage);

        assertThat(result.getUploader()).isSameAs(uploader);
        assertThat(result.getDirectory()).isEqualTo(RECEIPT);
        assertThat(result.getStorageKey()).isEqualTo(IMAGE_KEY);
        assertThat(result.getContentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        assertThat(result.getFileSize()).isEqualTo(validatedImage.size());
        assertThat(result.getStatus()).isEqualTo(ImageStatus.TEMPORARY);
        verify(imageRepository).save(result);
    }
}
