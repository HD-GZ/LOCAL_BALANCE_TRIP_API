package live.lbtrip.domain.savedcourse.service;

import static live.lbtrip.domain.savedcourse.model.ImagePurpose.RECEIPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.savedcourse.model.StoredImageStatus;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.StoredImage;
import live.lbtrip.domain.savedcourse.repository.StoredImageRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class StoredImageServiceTest {

    private static final Long IMAGE_ID = 1L;
    private static final Long SAVED_COURSE_ID = 2L;

    @Mock
    private StoredImageRepository storedImageRepository;

    @Mock
    private SavedCourse savedCourse;

    @InjectMocks
    private StoredImageService storedImageService;

    @Nested
    class 등록 {

        @Test
        void 영수증_이미지_업로드_정보를_저장한다() {
            when(storedImageRepository.save(org.mockito.ArgumentMatchers.any(StoredImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            StoredImage result = storedImageService.registerReceipt(
                savedCourse,
                "receipts/test.jpg",
                "image/jpeg",
                100
            );

            assertThat(result.getSavedCourse()).isSameAs(savedCourse);
            assertThat(result.getStatus()).isEqualTo(StoredImageStatus.PENDING);
            verify(storedImageRepository).save(result);
        }
    }

    @Nested
    class 사용 {

        @Test
        void 같은_저장_코스의_이미지를_증빙에_연결한다() {
            StoredImage image = StoredImage.createReceipt(
                savedCourse,
                "receipts/test.jpg",
                "image/jpeg",
                100
            );
            when(storedImageRepository.findByIdAndSavedCourseIdAndPurpose(
                IMAGE_ID,
                SAVED_COURSE_ID,
                RECEIPT
            )).thenReturn(Optional.of(image));

            StoredImage result = storedImageService.claimReceipt(IMAGE_ID, SAVED_COURSE_ID);

            assertThat(result.getStatus()).isEqualTo(StoredImageStatus.ATTACHED);
        }

        @Test
        void 다른_저장_코스의_이미지는_찾을_수_없다() {
            when(storedImageRepository.findByIdAndSavedCourseIdAndPurpose(
                IMAGE_ID,
                SAVED_COURSE_ID,
                RECEIPT
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storedImageService.claimReceipt(IMAGE_ID, SAVED_COURSE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECEIPT_IMAGE_NOT_FOUND);
        }
    }
}
