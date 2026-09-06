package live.lbtrip.domain.tourism.client;

import static live.lbtrip.support.fixture.GoogleTranslateTtsFixture.MP3_BYTES;
import static live.lbtrip.support.fixture.GoogleTranslateTtsFixture.RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.global.config.GoogleTranslateTtsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class GoogleTranslateTtsClientTest {

    private static final String BASE_URL = "https://translate.example.com";
    private static final String ENDPOINT = BASE_URL + "/_/TranslateWebserverUi/data/batchexecute";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockRestServiceServer server;
    private GoogleTranslateTtsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GoogleTranslateTtsClient(builder.build(), new GoogleTranslateTtsProperties(
            BASE_URL, Duration.ofSeconds(5), Duration.ofSeconds(10), Duration.ZERO));
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Nested
    class 음성_변환 {

        @Test
        void 한글과_특수문자를_전송하고_응답의_MP3를_반환한다() {
            String text = "안녕하세요. \"숲길\" & 여행 + 안내입니다.";
            expectSpeech(text, "ko");

            assertThat(client.synthesize("  " + text + "  ", Locale.KOREAN)).isEqualTo(MP3_BYTES);
        }

        @Test
        void 영문은_en_언어로_요청한다() {
            expectSpeech("A beautiful garden.", "en");

            assertThat(client.synthesize("A beautiful garden.", Locale.ENGLISH)).isEqualTo(MP3_BYTES);
        }

        @Test
        void 긴_문장은_단어_경계에서_분할하고_음성을_순서대로_합친다() {
            String first = "a".repeat(90);
            String second = "b".repeat(20);
            expectSpeech(first, "en");
            server.expect(requestTo(ENDPOINT))
                .andExpect(speechRequest(second, "en"))
                .andRespond(withSuccess(RESPONSE.replace("SUQz", "AQID"), MediaType.APPLICATION_JSON));

            assertThat(client.synthesize(first + " " + second, Locale.ENGLISH))
                .containsExactly(0x49, 0x44, 0x33, 1, 2, 3);
        }

        @Test
        void 공백_없는_긴_문장도_유니코드_문자를_깨뜨리지_않고_100자씩_분할한다() {
            String first = "가".repeat(99) + "🌲";
            expectSpeech(first, "ko");
            expectSpeech("나", "ko");

            assertThat(client.synthesize(first + "나", Locale.KOREAN))
                .containsExactly(0x49, 0x44, 0x33, 0x49, 0x44, 0x33);
        }

        @Test
        void 중간_조각이_실패하면_부분_음성을_반환하지_않는다() {
            expectSpeech("가".repeat(100), "ko");
            server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

            assertUnavailable(() -> client.synthesize("가".repeat(101), Locale.KOREAN));
        }
    }

    @Nested
    class 실패_처리 {

        @Test
        void 요청_제한은_재시도_없이_서비스_오류로_전파한다() {
            server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

            assertThatThrownBy(() -> client.synthesize("소개", Locale.KOREAN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOUR_API_UNAVAILABLE);
        }

        @Test
        void 타임아웃은_서비스_오류로_변환한다() {
            server.expect(requestTo(ENDPOINT)).andRespond(withException(new SocketTimeoutException()));

            assertUnavailable(() -> client.synthesize("소개", Locale.KOREAN));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "<html>blocked</html>", ")]}'\n42\n[[\"wrb.fr\",\"jQ1olc\",null]]"})
        void 음성이_없는_응답은_저장할_수_없다(String response) {
            server.expect(requestTo(ENDPOINT)).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

            assertUnavailable(() -> client.synthesize("소개", Locale.KOREAN));
        }

        @ParameterizedTest
        @ValueSource(strings = {"!!!", ""})
        void 잘못되거나_비어_있는_Base64_음성은_실패한다(String audio) {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(RESPONSE.replace("SUQz", audio), MediaType.APPLICATION_JSON));

            assertUnavailable(() -> client.synthesize("소개", Locale.KOREAN));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        void 빈_입력은_외부_요청_없이_거부한다(String input) {
            assertThatThrownBy(() -> client.synthesize(input, Locale.KOREAN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        void 인터럽트가_발생하면_외부_요청_없이_중단하고_플래그를_보존한다() {
            Thread.currentThread().interrupt();
            try {
                assertUnavailable(() -> client.synthesize("소개", Locale.KOREAN));
                assertThat(Thread.currentThread().isInterrupted()).isTrue();
            } finally {
                Thread.interrupted();
            }
        }
    }

    private void expectSpeech(String text, String language) {
        server.expect(requestTo(ENDPOINT))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(speechRequest(text, language))
            .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));
    }

    private RequestMatcher speechRequest(String text, String language) {
        return request -> {
            String form = ((MockClientHttpRequest) request).getBodyAsString();
            assertThat(form).startsWith("f.req=");
            String json = URLDecoder.decode(form.substring("f.req=".length()), StandardCharsets.UTF_8);
            JsonNode rpc = objectMapper.readTree(json).get(0).get(0);
            assertThat(rpc.get(0).asText()).isEqualTo("jQ1olc");
            JsonNode parameters = objectMapper.readTree(rpc.get(1).asText());
            assertThat(parameters.get(0).asText()).isEqualTo(text);
            assertThat(parameters.get(1).asText()).isEqualTo(language);
            assertThat(parameters.get(2).isNull()).isTrue();
            assertThat(parameters.get(3).asText()).isEqualTo("null");
        };
    }

    private void assertUnavailable(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.TOUR_API_UNAVAILABLE);
    }
}
