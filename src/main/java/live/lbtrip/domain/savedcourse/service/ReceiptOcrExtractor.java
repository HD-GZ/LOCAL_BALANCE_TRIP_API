package live.lbtrip.domain.savedcourse.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.savedcourse.model.vo.ReceiptOcrResult;
import live.lbtrip.global.storage.vo.ValidatedImage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReceiptOcrExtractor {

    private final ChatClient chatClient;
    private final String prompt;

    public ReceiptOcrExtractor(
        ChatClient.Builder chatClientBuilder,
        @Value("classpath:prompts/receipt-ocr.st") Resource promptResource
    ) {
        this.chatClient = chatClientBuilder.build();
        try {
            this.prompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ReceiptOcrResult extract(ValidatedImage image) {
        try {
            RawOcrResult raw = chatClient.prompt()
                .user(spec -> spec.text(prompt)
                    .media(image.mediaType(), new ByteArrayResource(image.bytes())))
                .call()
                .entity(RawOcrResult.class);
            return normalize(raw);
        } catch (Exception e) {
            log.warn("영수증 OCR 추출 실패", e);
            return ReceiptOcrResult.empty();
        }
    }

    private ReceiptOcrResult normalize(RawOcrResult raw) {
        if (raw == null) {
            return ReceiptOcrResult.empty();
        }
        return new ReceiptOcrResult(raw.merchantName(), raw.amount(), parseDate(raw.paidDate()));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            log.warn("영수증 결제일 파싱 실패: value={}", value);
            return null;
        }
    }

    record RawOcrResult(String merchantName, Integer amount, String paidDate) {

        static RawOcrResult of(String merchantName, Integer amount, String paidDate) {
            return new RawOcrResult(merchantName, amount, paidDate);
        }
    }
}
