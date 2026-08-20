package live.lbtrip.global.web;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String REQUEST_PATH = "requestPath";
    private static final String LANGUAGE = "Language";
    private static final String MASKED_HEADER_VALUE = "[MASKED]";
    private static final int MAX_LOG_VALUE_LENGTH = 500;
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
        "authorization",
        "cookie",
        "proxy-authorization",
        "set-cookie",
        "x-api-key"
    );

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        String httpMethod = request.getMethod();
        String requestPath = sanitizeLogValue(request.getRequestURI());
        long startedAt = System.nanoTime();

        MDC.put(REQUEST_ID, requestId);
        MDC.put(HTTP_METHOD, httpMethod);
        MDC.put(REQUEST_PATH, requestPath);
        putIfPresent(LANGUAGE, request.getHeader("Accept-Language"));

        log.info(
            "HTTP request started: requestId={}, method={}, path={}, headers={}",
            requestId,
            httpMethod,
            requestPath,
            formatHeaders(request)
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info(
                "HTTP request completed: requestId={}, method={}, path={}, status={}, elapsedMs={}",
                requestId,
                httpMethod,
                requestPath,
                response.getStatus(),
                elapsedMillis(startedAt)
            );
            MDC.remove(REQUEST_ID);
            MDC.remove(HTTP_METHOD);
            MDC.remove(REQUEST_PATH);
            MDC.remove(LANGUAGE);
        }
    }

    private String formatHeaders(HttpServletRequest request) {
        if (request.getHeaderNames() == null) {
            return "{}";
        }

        return Collections.list(request.getHeaderNames()).stream()
            .map(headerName -> formatHeader(request, headerName))
            .collect(Collectors.joining(", ", "{", "}"));
    }

    private String formatHeader(HttpServletRequest request, String headerName) {
        if (isSensitiveHeader(headerName)) {
            return sanitizeLogValue(headerName) + "=" + MASKED_HEADER_VALUE;
        }

        List<String> headerValues = Collections.list(request.getHeaders(headerName)).stream()
            .map(this::sanitizeLogValue)
            .toList();
        return sanitizeLogValue(headerName) + "=" + headerValues;
    }

    private boolean isSensitiveHeader(String headerName) {
        String normalizedHeaderName = headerName.toLowerCase(Locale.ROOT);
        return SENSITIVE_HEADERS.contains(normalizedHeaderName)
            || normalizedHeaderName.contains("token")
            || normalizedHeaderName.contains("secret");
    }

    private void putIfPresent(String key, String value) {
        if (value != null) {
            MDC.put(key, sanitizeLogValue(value));
        }
    }

    private String sanitizeLogValue(String value) {
        String sanitizedValue = value.replace('\n', '_').replace('\r', '_');
        if (sanitizedValue.length() <= MAX_LOG_VALUE_LENGTH) {
            return sanitizedValue;
        }
        return sanitizedValue.substring(0, MAX_LOG_VALUE_LENGTH);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
