# Project Coding Agent Rules

이 문서는 이 저장소에서 코딩 에이전트가 따라야 할 프로젝트 규칙의 단일 원본이다.
`CLAUDE.md` 등 다른 에이전트 전용 규칙 파일을 만들 때는 이 문서를 복제하지 말고 이 파일을 참조하도록 작성한다.

## 범위

- Java/Spring Boot API 코드는 `src/main/java/live/lbtrip` 아래의 기존 패키지 구조를 따른다.

## API 계층

- 컨트롤러 구현체는 라우팅, 요청 검증 애너테이션, 서비스 호출, `ResponseEntity` 생성만 담당한다.
- Swagger 문서화는 컨트롤러 구현체가 아니라 `*Api` 인터페이스에 작성한다.
- 신규 API를 추가할 때는 같은 패키지에 `XxxApi` 인터페이스를 만들고, `XxxController implements XxxApi` 구조를 따른다.
- `*Api` 메서드에는 `@Operation`, `@ApiSuccessResponse`, `@ApiErrorCodeResponses`를 작성한다.
- 인증이 필요한 API에는 `@SecurityRequirement(name = "bearerAuth")`와 필요한 경우 `@UserId`를 사용한다.

## 응답과 예외

- 모든 JSON API 응답은 `ApiResponse` 래핑 구조를 따른다.
- 일반 성공 응답은 컨트롤러에서 직접 `ApiResponse.success(...)`로 감싸지 않는다. `ApiResponseAdvice`가 자동으로 래핑한다.
- 비즈니스 예외는 `BusinessException.of(ErrorCode.X)`로 던진다.
- 새로운 비즈니스 예외가 필요하면 먼저 `ErrorCode`에 HTTP 상태와 한국어 메시지를 추가한다.
- 임의의 `RuntimeException`, 하드코딩된 에러 응답, 컨트롤러 내부 예외 응답 생성을 피한다.
- validation 실패는 `GlobalExceptionHandler`의 `INVALID_INPUT_VALUE` 응답 흐름을 사용한다.

## 서비스 계층

- 서비스 클래스에는 기본적으로 `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)`를 사용한다.
- 저장, 수정, 삭제가 있는 메서드에만 별도로 `@Transactional`을 붙인다.
- 사용자 입력 문자열은 서비스 진입 지점에서 정규화한다.
- 이메일 조회/중복 체크 전에는 `StringNormalizer.trimToLowerCase(...)`를 사용한다.
- 이름, 인증 코드 등 단순 문자열은 필요한 경우 `StringNormalizer.trim(...)`을 사용한다.
- 서비스는 도메인 객체의 상태를 직접 세팅하기보다 엔티티 메서드를 호출한다.

## DTO 규칙

- 요청/응답 DTO는 `record`로 작성한다.
- 요청 DTO에는 Bean Validation 애너테이션과 한국어 validation 메시지를 작성한다.
- 요청 body DTO에는 `@Schema`를 사용하고, query/model attribute DTO에는 필요에 따라 `@Parameter`를 사용한다.
- 응답 DTO는 엔티티를 그대로 노출하지 않고 필요한 필드만 담는다.
- 응답 DTO 생성은 `from(...)` 또는 `of(...)` 정적 팩토리 메서드를 사용한다.

## 도메인과 JPA

- JPA 엔티티는 `@Getter`, `@Entity`, `@Table`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 패턴을 따른다.
- 엔티티 생성자는 `private`으로 두고, 외부 생성은 `create(...)` 정적 팩토리로 제한한다.
- 신규 엔티티는 특별한 이유가 없으면 `BaseEntity`를 상속해 `created_at`, `updated_at`을 사용한다.
- 연관관계는 기본적으로 지연 로딩(`FetchType.LAZY`)을 우선한다.
- enum 필드는 `@Enumerated(EnumType.STRING)`을 사용한다.
- 도메인 검증과 상태 변경은 엔티티 메서드에 둔다.
- 도메인 규칙 위반도 `BusinessException.of(ErrorCode.X)`를 사용한다.

## 데이터베이스

- 스키마 변경은 Flyway 마이그레이션 파일로 관리한다.
- 신규 마이그레이션은 `src/main/resources/db/migration`에 `V{번호}__{설명}.sql` 형식으로 추가한다.
- 엔티티 컬럼 제약과 마이그레이션의 `NOT NULL`, 길이, unique 제약이 서로 어긋나지 않게 유지한다.
- `BaseEntity`를 상속하는 테이블에는 `created_at`, `updated_at` 컬럼을 포함한다.

## 테스트

- 컨트롤러 테스트는 `@WebMvcTest`와 `MockMvc`를 사용한다.
- 컨트롤러 테스트에서는 HTTP status뿐 아니라 `$.result`, `$.data`, `$.error.code` 등 공통 응답 포맷을 검증한다.
- 서비스 테스트는 `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` 기반 단위 테스트를 우선한다.
- 비즈니스 예외 테스트는 예외 타입뿐 아니라 `errorCode`까지 검증한다.
- 테스트 데이터는 `src/test/java/live/lbtrip/support/fixture`의 fixture 클래스를 우선 사용하고, 중복 하드코딩을 피한다.
- 테스트 이름은 기존처럼 한국어 메서드명과 `@Nested` 그룹을 사용한다.

## 스타일

- 기존 패키지 구조(`domain/{도메인}/controller|service|model|repository|dto`, `global/*`)를 유지한다.
- Lombok은 기존 패턴에 맞춰 사용하되, 엔티티에는 setter를 만들지 않는다.
- 새 공통 기능은 `global` 아래에 두고, 특정 도메인 전용 로직은 해당 `domain` 패키지 안에 둔다.
- 운영 설정값은 `application.yml`에서 환경변수로 받고, 테스트 전용 값은 `application-test.yml`에 둔다.
