package live.lbtrip.global.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "live.lbtrip")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !StringHttpMessageConverter.class.isAssignableFrom(converterType)
            && !ResourceHttpMessageConverter.class.isAssignableFrom(converterType)
            && !ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
        Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest request,
        ServerHttpResponse response
    ) {
        if (body instanceof ApiResponse<?>) {
            return body;
        }

        if (isNoContent(response) || !isJsonResponse(selectedContentType)) {
            return body;
        }

        return body == null ? ApiResponse.success() : ApiResponse.success(body);
    }

    private boolean isNoContent(ServerHttpResponse response) {
        return response instanceof ServletServerHttpResponse servletResponse
            && servletResponse.getServletResponse().getStatus() == HttpStatus.NO_CONTENT.value();
    }

    private boolean isJsonResponse(MediaType selectedContentType) {
        return selectedContentType == null
            || MediaType.APPLICATION_JSON.includes(selectedContentType)
            || selectedContentType.includes(MediaType.APPLICATION_JSON);
    }
}
