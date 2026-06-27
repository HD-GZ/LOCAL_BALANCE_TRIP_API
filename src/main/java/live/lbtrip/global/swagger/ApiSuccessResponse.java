package live.lbtrip.global.swagger;

import static org.springframework.http.HttpStatus.OK;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.HttpStatus;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSuccessResponse {

    HttpStatus status() default OK;

    String description() default "요청 성공";
}
