package live.lbtrip.domain.tourism.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.global.config.TourApiProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicDataClient {

    private static final String RESULT_OK = "0000";

    private final TourApiProperties properties;
    private final ConcurrentMap<String, RestClient> clientsByBaseUrl = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode get(String baseUrl, String path, UnaryOperator<UriBuilder> customizer) {
        try {
            String raw = clientFor(baseUrl).get()
                .uri(uriBuilder -> customizer.apply(uriBuilder
                        .path(path)
                        .queryParam("serviceKey", properties.serviceKey())
                        .queryParam("MobileOS", properties.mobileOs())
                        .queryParam("MobileApp", properties.mobileApp())
                        .queryParam("_type", "json")
                        .queryParam("pageNo", 1))
                    .build())
                .retrieve()
                .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!RESULT_OK.equals(resultCode)) {
                log.error("공공데이터 API 오류 응답: path={}, resultCode={}", path, resultCode);
                throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
            }
            return root.path("response").path("body");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("공공데이터 API 호출 실패: path={}", path, e);
            throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
        }
    }

    public JsonNode items(JsonNode body) {
        JsonNode item = body.path("items").path("item");
        if (item.isArray()) {
            return item;
        }
        return objectMapper.createArrayNode();
    }

    private RestClient clientFor(String baseUrl) {
        return clientsByBaseUrl.computeIfAbsent(baseUrl, this::createClient);
    }

    private RestClient createClient(String baseUrl) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(baseUrl);
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        ReactorClientHttpRequestFactory requestFactory = new ReactorClientHttpRequestFactory();
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder()
            .uriBuilderFactory(uriFactory)
            .requestFactory(requestFactory)
            .build();
    }
}
