package live.lbtrip.global.swagger;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import live.lbtrip.global.error.ErrorCode;

@Component
public class ApiResponseOperationCustomizer implements OperationCustomizer {

    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_ERROR = "ERROR";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        findAnnotation(handlerMethod, ApiSuccessResponse.class)
            .ifPresent(successResponse -> applySuccessResponse(operation, handlerMethod, successResponse));

        findAnnotation(handlerMethod, ApiErrorCodeResponses.class)
            .ifPresent(errorResponses -> applyErrorResponses(operation, errorResponses));

        return operation;
    }

    private void applySuccessResponse(
        Operation operation,
        HandlerMethod handlerMethod,
        ApiSuccessResponse successResponse
    ) {
        ApiResponses responses = getResponses(operation);
        String responseCode = String.valueOf(successResponse.status().value());
        Schema<?> dataSchema = findExistingSuccessDataSchema(responses)
            .orElseGet(() -> resolveDataSchema(handlerMethod.getMethod().getGenericReturnType()));

        removeSuccessResponses(responses);
        responses.addApiResponse(responseCode, new ApiResponse()
            .description(successResponse.description())
            .content(jsonContent(successSchema(dataSchema))));
    }

    private void applyErrorResponses(Operation operation, ApiErrorCodeResponses errorResponses) {
        ApiResponses responses = getResponses(operation);
        Map<HttpStatus, List<ErrorCode>> errorCodesByStatus = Arrays.stream(errorResponses.value())
            .collect(Collectors.groupingBy(ErrorCode::getStatus, LinkedHashMap::new, Collectors.toList()));

        errorCodesByStatus.forEach((status, errorCodes) -> responses.addApiResponse(
            String.valueOf(status.value()),
            new ApiResponse()
                .description(errorDescription(errorCodes))
                .content(errorContent(errorCodes))
        ));
    }

    private ApiResponses getResponses(Operation operation) {
        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }
        return operation.getResponses();
    }

    private Optional<Schema<?>> findExistingSuccessDataSchema(ApiResponses responses) {
        return responses.entrySet().stream()
            .filter(entry -> isSuccessResponseCode(entry.getKey()))
            .map(entry -> entry.getValue().getContent())
            .filter(Objects::nonNull)
            .map(content -> content.get(APPLICATION_JSON_VALUE))
            .filter(Objects::nonNull)
            .map(io.swagger.v3.oas.models.media.MediaType::getSchema)
            .filter(Objects::nonNull)
            .<Schema<?>>map(schema -> schema)
            .findFirst();
    }

    private void removeSuccessResponses(ApiResponses responses) {
        List<String> successResponseCodes = responses.keySet().stream()
            .filter(this::isSuccessResponseCode)
            .toList();
        successResponseCodes.forEach(responses::remove);
    }

    private boolean isSuccessResponseCode(String responseCode) {
        try {
            int code = Integer.parseInt(responseCode);
            return code >= 200 && code < 300;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private Schema<?> resolveDataSchema(Type returnType) {
        Type bodyType = extractResponseEntityBodyType(returnType);
        if (bodyType == Void.class || bodyType == Void.TYPE) {
            return nullSchema();
        }

        Schema<?> schema = ModelConverters.getInstance()
            .resolveAsResolvedSchema(new AnnotatedType(bodyType))
            .schema;

        return schema == null ? new ObjectSchema() : schema;
    }

    private Type extractResponseEntityBodyType(Type returnType) {
        if (returnType instanceof ParameterizedType parameterizedType
            && parameterizedType.getRawType() == ResponseEntity.class) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        return returnType;
    }

    private Content jsonContent(Schema<?> schema) {
        return new Content().addMediaType(
            APPLICATION_JSON_VALUE,
            new io.swagger.v3.oas.models.media.MediaType().schema(schema)
        );
    }

    private Schema<?> successSchema(Schema<?> dataSchema) {
        return new ObjectSchema()
            .addProperty("result", resultSchema(RESULT_SUCCESS))
            .addProperty("data", dataSchema)
            .addProperty("error", nullSchema());
    }

    private Content errorContent(List<ErrorCode> errorCodes) {
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType()
            .schema(errorSchema());

        errorCodes.forEach(errorCode -> mediaType.addExamples(
            errorCode.name(),
            new Example().value(errorExample(errorCode))
        ));

        return new Content().addMediaType(APPLICATION_JSON_VALUE, mediaType);
    }

    private Schema<?> errorSchema() {
        return new ObjectSchema()
            .addProperty("result", resultSchema(RESULT_ERROR))
            .addProperty("data", nullSchema())
            .addProperty("error", new ObjectSchema()
                .addProperty("code", new StringSchema().example("INVALID_INPUT_VALUE"))
                .addProperty("message", new StringSchema().example("입력값이 올바르지 않습니다."))
                .addProperty("data", new ObjectSchema().nullable(true)));
    }

    private Schema<?> resultSchema(String result) {
        return new StringSchema()
            ._enum(List.of(result))
            .example(result);
    }

    private Schema<?> nullSchema() {
        return new ObjectSchema()
            .nullable(true)
            .example(null);
    }

    private String errorDescription(List<ErrorCode> errorCodes) {
        return errorCodes.stream()
            .map(errorCode -> errorCode.name() + ": " + errorCode.getMessage())
            .collect(Collectors.joining("<br/>"));
    }

    private Map<String, Object> errorExample(ErrorCode errorCode) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", errorCode.name());
        error.put("message", errorCode.getMessage());
        error.put("data", errorDataExample(errorCode));

        Map<String, Object> example = new LinkedHashMap<>();
        example.put("result", RESULT_ERROR);
        example.put("data", null);
        example.put("error", error);
        return example;
    }

    private Object errorDataExample(ErrorCode errorCode) {
        if (errorCode != ErrorCode.INVALID_INPUT_VALUE) {
            return null;
        }
        return List.of(Map.of(
            "field", "email",
            "message", "이메일 형식이 올바르지 않습니다."
        ));
    }

    private <A extends java.lang.annotation.Annotation> Optional<A> findAnnotation(
        HandlerMethod handlerMethod,
        Class<A> annotationType
    ) {
        A annotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), annotationType);
        if (annotation != null) {
            return Optional.of(annotation);
        }

        return findInterfaceMethod(handlerMethod)
            .map(method -> AnnotatedElementUtils.findMergedAnnotation(method, annotationType));
    }

    private Optional<Method> findInterfaceMethod(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        Class<?> beanType = handlerMethod.getBeanType();

        return Arrays.stream(beanType.getInterfaces())
            .map(interfaceType -> findMatchingMethod(interfaceType, method))
            .flatMap(Optional::stream)
            .findFirst();
    }

    private Optional<Method> findMatchingMethod(Class<?> interfaceType, Method targetMethod) {
        return Arrays.stream(interfaceType.getMethods())
            .filter(method -> method.getName().equals(targetMethod.getName()))
            .filter(method -> Arrays.equals(method.getParameterTypes(), targetMethod.getParameterTypes()))
            .findFirst();
    }
}
