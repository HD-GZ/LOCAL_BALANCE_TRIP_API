package live.lbtrip.domain.savedcourse.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.savedcourse.model.StoredImageStatus;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class StoredImageTest {

    @Nested
    class 증빙_연결 {

        @Test
        void 대기_이미지를_증빙에_연결한다() {
            StoredImage image = StoredImage.createReceipt(
                mock(SavedCourse.class),
                "receipts/test.jpg",
                "image/jpeg",
                100
            );

            image.attach();

            assertThat(image.getStatus()).isEqualTo(StoredImageStatus.ATTACHED);
        }

        @Test
        void 이미_연결된_이미지는_다시_연결할_수_없다() {
            StoredImage image = StoredImage.createReceipt(
                mock(SavedCourse.class),
                "receipts/test.jpg",
                "image/jpeg",
                100
            );
            image.attach();

            assertThatThrownBy(image::attach)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RECEIPT_IMAGE_ALREADY_USED);
        }
    }
}
