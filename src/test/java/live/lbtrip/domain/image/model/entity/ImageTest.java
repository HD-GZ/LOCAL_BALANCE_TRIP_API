package live.lbtrip.domain.image.model.entity;

import static live.lbtrip.global.storage.ImageDirectory.RECEIPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.image.model.ImageStatus;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class ImageTest {

    @Nested
    class 도메인_연결 {

        @Test
        void 임시_이미지를_도메인에_연결한다() {
            Image image = image();

            image.attach();

            assertThat(image.getStatus()).isEqualTo(ImageStatus.ATTACHED);
        }

        @Test
        void 이미_연결된_이미지는_다시_연결할_수_없다() {
            Image image = image();
            image.attach();

            assertThatThrownBy(image::attach)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_ALREADY_ATTACHED);
        }
    }

    private Image image() {
        return Image.create(
            mock(User.class),
            RECEIPT,
            "receipts/test.jpg",
            "image/jpeg",
            100
        );
    }
}
