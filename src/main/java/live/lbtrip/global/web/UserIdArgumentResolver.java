package live.lbtrip.global.web;

import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(UserId.class)
			&& Long.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Long resolveArgument(
		MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) {
		String authorization = webRequest.getHeader(AUTHORIZATION_HEADER);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			throw BusinessException.of(ErrorCode.INVALID_ACCESS_TOKEN);
		}

		String token = authorization.substring(BEARER_PREFIX.length());
		if (!jwtTokenProvider.isValid(token)) {
			throw BusinessException.of(ErrorCode.INVALID_ACCESS_TOKEN);
		}

		return jwtTokenProvider.parseSubject(token).userId();
	}
}
