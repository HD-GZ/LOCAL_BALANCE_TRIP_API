package live.lbtrip.global.web;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String REQUEST_PATH = "requestPath";
    private static final int MAX_PATH_LENGTH = 500;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        MDC.put(HTTP_METHOD, request.getMethod());
        MDC.put(REQUEST_PATH, sanitizePath(request.getRequestURI()));

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID);
            MDC.remove(HTTP_METHOD);
            MDC.remove(REQUEST_PATH);
        }
    }

    private String sanitizePath(String path) {
        String sanitizedPath = path.replace('\n', '_').replace('\r', '_');
        if (sanitizedPath.length() <= MAX_PATH_LENGTH) {
            return sanitizedPath;
        }
        return sanitizedPath.substring(0, MAX_PATH_LENGTH);
    }
}
