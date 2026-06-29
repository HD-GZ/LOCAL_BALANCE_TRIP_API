# Project Coding Agent Rules

This document is the single source of truth for coding agent rules in this repository.
When adding agent-specific rule files such as `CLAUDE.md`, do not duplicate these rules; link to this file instead.

## Scope

- Java/Spring Boot API code should follow the existing package structure under `src/main/java/live/lbtrip`.

## API Layer

- Controller implementations should only handle routing, request validation annotations, service calls, and `ResponseEntity` creation.
- Put Swagger documentation in `*Api` interfaces, not in controller implementations.
- When adding a new API, create an `XxxApi` interface in the same package and use the `XxxController implements XxxApi` structure.
- Add `@Operation`, `@ApiSuccessResponse`, and `@ApiErrorCodeResponses` to `*Api` methods.
- For authenticated APIs, use `@SecurityRequirement(name = "bearerAuth")` and `@UserId` when needed.

## Responses and Exceptions

- All JSON API responses must follow the `ApiResponse` wrapper structure.
- Do not wrap normal success responses manually with `ApiResponse.success(...)` in controllers. `ApiResponseAdvice` performs wrapping automatically.
- Throw business exceptions with `BusinessException.of(ErrorCode.X)`.
- When a new business error is needed, add an `ErrorCode` entry first with the HTTP status and Korean message.
- Avoid arbitrary `RuntimeException`s, hard-coded error responses, and controller-local exception response creation.
- Validation failures should use the `INVALID_INPUT_VALUE` response flow in `GlobalExceptionHandler`.

## Service Layer

- Service classes should generally use `@Service`, `@RequiredArgsConstructor`, and `@Transactional(readOnly = true)`.
- Add method-level `@Transactional` only to methods that create, update, or delete data.
- Normalize user-input strings at the service boundary.
- Before email lookup or duplicate checks, use `StringNormalizer.trimToLowerCase(...)`.
- For simple strings such as names and verification codes, use `StringNormalizer.trim(...)` when normalization is needed.
- Services should call entity methods for domain state changes instead of setting entity state directly.

## DTO Rules

- Use `record` for request and response DTOs.
- Request DTOs should include Bean Validation annotations and Korean validation messages.
- Use `@Schema` for request body DTOs. Use `@Parameter` where appropriate for query or model attribute DTOs.
- Response DTOs should expose only the fields needed by the API, not whole entities.
- Create response DTOs through `from(...)` or `of(...)` static factory methods.

## Domain and JPA

- JPA entities should follow the `@Getter`, `@Entity`, `@Table`, and `@NoArgsConstructor(access = AccessLevel.PROTECTED)` pattern.
- Keep entity constructors `private` and expose creation through `create(...)` static factory methods.
- New entities should extend `BaseEntity` and use `created_at` and `updated_at` unless there is a specific reason not to.
- Prefer lazy loading (`FetchType.LAZY`) for associations.
- Use `@Enumerated(EnumType.STRING)` for enum fields.
- Put domain validation and state changes in entity methods.
- Domain rule violations should also use `BusinessException.of(ErrorCode.X)`.

## Database

- Manage schema changes with Flyway migration files.
- Add new migrations under `src/main/resources/db/migration` using the `V{number}__{description}.sql` naming format.
- Keep entity column constraints aligned with migration constraints such as `NOT NULL`, length, and unique constraints.
- Tables for entities that extend `BaseEntity` must include `created_at` and `updated_at` columns.

## Tests

- Use `@WebMvcTest` and `MockMvc` for controller tests.
- Controller tests should verify the common response format, including fields such as `$.result`, `$.data`, and `$.error.code`, in addition to HTTP status.
- Prefer unit tests with `@ExtendWith(MockitoExtension.class)`, `@Mock`, and `@InjectMocks` for service tests.
- Business exception tests should verify both the exception type and the `errorCode`.
- Prefer fixture classes under `src/test/java/live/lbtrip/support/fixture` for test data and avoid duplicated hard-coded values.
- Follow the existing style of Korean test method names and `@Nested` test grouping.

## Style

- Preserve the existing package structure: `domain/{domain}/controller|service|model|repository|dto` and `global/*`.
- Use Lombok consistently with the existing patterns, but do not add setters to entities.
- Put new shared functionality under `global`; keep domain-specific logic inside the relevant `domain` package.
- Read production configuration values from environment variables through `application.yml`, and keep test-only values in `application-test.yml`.
