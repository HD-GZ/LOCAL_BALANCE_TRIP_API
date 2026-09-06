package live.lbtrip.domain.tourism.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import live.lbtrip.global.config.GoogleTranslateTtsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Calls the undocumented Google Translate speech RPC used by gTTS.
 * Protocol reference: https://gtts.readthedocs.io/en/latest/_modules/gtts/tts.html
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTranslateTtsClient {

    private static final String SPEECH_PATH = "/_/TranslateWebserverUi/data/batchexecute";
    private static final String SPEECH_RPC = "jQ1olc";
    private static final int MAX_CHUNK_CODE_POINTS = 100;

    private final RestClient googleTranslateTtsRestClient;
    private final GoogleTranslateTtsProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] synthesize(String text, Locale locale) {
        String input = text == null ? "" : StringNormalizer.trim(text);
        if (input.isEmpty() || locale == null || locale.getLanguage().isEmpty()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            ByteArrayOutputStream audio = new ByteArrayOutputStream();
            int start = 0;
            while (start < input.length()) {
                int end = chunkEnd(input, start);
                String chunk = input.substring(start, end).trim();
                if (!chunk.isEmpty()) {
                    Thread.sleep(properties.requestInterval());
                    audio.writeBytes(requestSpeech(chunk, locale.getLanguage()));
                }
                start = end;
            }
            return audio.toByteArray();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
        } catch (Exception e) {
            log.warn("구글 번역 TTS 호출 실패: locale={}", locale, e);
            throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
        }
    }

    private int chunkEnd(String text, int start) {
        int remaining = text.codePointCount(start, text.length());
        int end = text.offsetByCodePoints(start, Math.min(remaining, MAX_CHUNK_CODE_POINTS));
        if (end == text.length()) {
            return end;
        }
        for (int boundary = end; boundary > start; boundary = text.offsetByCodePoints(boundary, -1)) {
            if (Character.isWhitespace(text.codePointBefore(boundary))) {
                return boundary;
            }
        }
        return end;
    }

    private byte[] requestSpeech(String text, String language) throws IOException {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("f.req", requestPayload(text, language));
        String response = googleTranslateTtsRestClient.post()
            .uri(SPEECH_PATH)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(String.class);
        return decodeAudio(response);
    }

    private String requestPayload(String text, String language) throws JsonProcessingException {
        ArrayNode parameters = objectMapper.createArrayNode()
            .add(text).add(language).addNull().add("null");
        ArrayNode rpc = objectMapper.createArrayNode()
            .add(SPEECH_RPC).add(objectMapper.writeValueAsString(parameters)).addNull().add("generic");
        ArrayNode request = objectMapper.createArrayNode()
            .add(objectMapper.createArrayNode().add(rpc));
        return objectMapper.writeValueAsString(request);
    }

    private byte[] decodeAudio(String response) throws IOException {
        if (response != null) {
            // batchexecute prefixes JSON frames with an XSSI marker and byte counts.
            for (String line : response.split("\\R")) {
                if (!line.stripLeading().startsWith("[")) {
                    continue;
                }
                JsonNode entries = objectMapper.readTree(line);
                for (JsonNode entry : entries) {
                    if (!SPEECH_RPC.equals(entry.path(1).asText()) || !entry.path(2).isTextual()) {
                        continue;
                    }
                    JsonNode payload = objectMapper.readTree(entry.path(2).asText());
                    if (payload == null || !payload.path(0).isTextual()) {
                        continue;
                    }
                    byte[] audio = Base64.getDecoder().decode(payload.path(0).asText());
                    if (audio.length > 0) {
                        return audio;
                    }
                }
            }
        }
        throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
    }
}
