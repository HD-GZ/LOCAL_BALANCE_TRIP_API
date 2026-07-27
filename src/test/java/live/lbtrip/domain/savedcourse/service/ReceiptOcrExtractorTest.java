package live.lbtrip.domain.savedcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import live.lbtrip.domain.savedcourse.model.vo.ReceiptOcrResult;
import live.lbtrip.global.storage.vo.ValidatedImage;

@ExtendWith(MockitoExtension.class)
class ReceiptOcrExtractorTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private ReceiptOcrExtractor receiptOcrExtractor;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        receiptOcrExtractor = new ReceiptOcrExtractor(
            chatClientBuilder,
            new ByteArrayResource("영수증을 분석하세요.".getBytes(StandardCharsets.UTF_8))
        );
    }

    @Nested
    class 추출 {

        @Test
        void OCR_결과를_정규화한다() {
            mockOcrResponse(ReceiptOcrExtractor.RawOcrResult.of(
                "국수거리 노포",
                18000,
                "2026-07-17"
            ));

            ReceiptOcrResult result = receiptOcrExtractor.extract(image());

            assertThat(result.merchantName()).isEqualTo("국수거리 노포");
            assertThat(result.amount()).isEqualTo(18000);
            assertThat(result.paidDate()).isEqualTo(LocalDate.of(2026, 7, 17));
        }

        @Test
        void 결제일을_파싱할_수_없으면_해당_필드만_null로_반환한다() {
            mockOcrResponse(ReceiptOcrExtractor.RawOcrResult.of(
                "국수거리 노포",
                18000,
                "잘못된 날짜"
            ));

            ReceiptOcrResult result = receiptOcrExtractor.extract(image());

            assertThat(result.merchantName()).isEqualTo("국수거리 노포");
            assertThat(result.amount()).isEqualTo(18000);
            assertThat(result.paidDate()).isNull();
        }

        @Test
        void OCR_호출에_실패하면_모든_필드를_null로_반환한다() {
            when(chatClient.prompt()).thenThrow(new RuntimeException("OCR failed"));

            ReceiptOcrResult result = receiptOcrExtractor.extract(image());

            assertThat(result.merchantName()).isNull();
            assertThat(result.amount()).isNull();
            assertThat(result.paidDate()).isNull();
        }
    }

    @SuppressWarnings("unchecked")
    private void mockOcrResponse(ReceiptOcrExtractor.RawOcrResult rawResult) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(ReceiptOcrExtractor.RawOcrResult.class)).thenReturn(rawResult);
    }

    private ValidatedImage image() {
        return ValidatedImage.of(
            new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "jpg",
            MediaType.IMAGE_JPEG
        );
    }
}
