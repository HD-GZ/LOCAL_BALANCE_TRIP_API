package live.lbtrip.global.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserIdArgumentResolverTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private MethodParameter parameter;
    @Mock private NativeWebRequest webRequest;
    @InjectMocks private UserIdArgumentResolver resolver;

    @Test
    void 토큰이_없고_required가_false면_null을_반환한다() throws Exception {
        when(webRequest.getHeader("Authorization")).thenReturn(null);
        when(parameter.getParameterAnnotation(UserId.class)).thenReturn(userId(false));

        Long result = resolver.resolveArgument(parameter, null, webRequest, null);

        assertThat(result).isNull();
    }

    @Test
    void 토큰이_없고_required가_true면_예외를_던진다() throws Exception {
        when(webRequest.getHeader("Authorization")).thenReturn(null);
        when(parameter.getParameterAnnotation(UserId.class)).thenReturn(userId(true));

        assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    void 유효한_토큰이면_userId를_반환한다() throws Exception {
        when(webRequest.getHeader("Authorization")).thenReturn("Bearer access-token");
        when(parameter.getParameterAnnotation(UserId.class)).thenReturn(userId(false));
        when(jwtTokenProvider.isValid("access-token")).thenReturn(true);
        when(jwtTokenProvider.parseSubject("access-token")).thenReturn(new JwtTokenSubject(7L));

        Long result = resolver.resolveArgument(parameter, null, webRequest, null);

        assertThat(result).isEqualTo(7L);
    }

    private UserId userId(boolean required) {
        return new UserId() {
            @Override public Class<? extends Annotation> annotationType() { return UserId.class; }
            @Override public boolean required() { return required; }
        };
    }
}
