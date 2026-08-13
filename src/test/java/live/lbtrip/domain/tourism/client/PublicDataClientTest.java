package live.lbtrip.domain.tourism.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class PublicDataClientTest {

    private final PublicDataClient publicDataClient = new PublicDataClient(null);

    @Nested
    class 한도_초과_전파 {

        @Test
        void 한도_초과_예외는_다시_던진다() {
            BusinessException quotaExceeded = BusinessException.of(ErrorCode.TOUR_API_QUOTA_EXCEEDED);

            assertThatThrownBy(() -> publicDataClient.rethrowIfQuotaExceeded(quotaExceeded))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOUR_API_QUOTA_EXCEEDED);
        }

        @Test
        void 다른_비즈니스_예외는_던지지_않는다() {
            BusinessException unavailable = BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);

            assertThatCode(() -> publicDataClient.rethrowIfQuotaExceeded(unavailable))
                .doesNotThrowAnyException();
        }

        @Test
        void 비즈니스_예외가_아니면_던지지_않는다() {
            assertThatCode(() -> publicDataClient.rethrowIfQuotaExceeded(new RuntimeException("timeout")))
                .doesNotThrowAnyException();
        }
    }
}
