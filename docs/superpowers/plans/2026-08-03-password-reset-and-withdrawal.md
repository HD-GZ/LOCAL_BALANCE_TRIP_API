# 비밀번호 찾기 · 회원탈퇴 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 이메일 인증 기반 비밀번호 재설정 API 3개와, 30일 유예기간을 갖는 소프트 삭제 회원탈퇴(로그인 시 자동 철회 + 일 1회 익명화 스케줄러)를 구현한다.

**Architecture:** 스펙 `docs/superpowers/specs/2026-08-03-password-reset-and-withdrawal-design.md` 기준. 비밀번호 재설정은 `password_reset_tokens` 한 행이 "코드 발급 → 코드 확인(resetToken 발급) → 사용 완료"의 생명주기를 표현한다. 탈퇴는 `users.status=WITHDRAWN` + `withdrawn_at`으로 소프트 삭제하고, 스케줄러가 유예기간 경과 유저를 익명화(`deleted_at` 기록)하며 연관 데이터를 삭제한다. 기존 `email_verification_tokens`는 `signup_verification_tokens`로 리네임한다.

**Tech Stack:** Java 21 / Spring Boot / Spring Data JPA / Flyway(MySQL) / SendGrid / JUnit5 + Mockito + MockMvc (테스트는 H2, `ddl-auto: create-drop`, Flyway 비활성)

## Global Constraints

- CLAUDE.md 컨벤션 준수: Controller는 라우팅·검증·서비스 호출·ResponseEntity만, Swagger는 `*Api` 인터페이스에, DTO는 record + 한국어 검증 메시지, 엔티티는 `@NoArgsConstructor(PROTECTED)` + private 생성자 + `create(...)` 정적 팩토리, 상태 변경은 엔티티 메서드로.
- **코드 주석 절대 금지** (javadoc·인라인 모두). 설명은 커밋 메시지와 문서로만.
- 비즈니스 예외는 `BusinessException.of(ErrorCode.X)`. 신규 에러는 `ErrorCode`에 HTTP 상태 + 한국어 메시지로 먼저 추가.
- 이메일은 `StringNormalizer.trimToLowerCase(...)`, 코드·토큰 문자열은 `StringNormalizer.trim(...)`으로 정규화.
- 테스트: 한국어 메서드명 + `@Nested` 그룹핑. 컨트롤러는 `@WebMvcTest`로 `$.result`/`$.data`/`$.error.code` 검증, 서비스는 `MockitoExtension` 단위 테스트. 픽스처는 `src/test/java/live/lbtrip/support/fixture` 사용.
- 전체 테스트 실행: `./gradlew test`. 단일 클래스: `./gradlew test --tests "live.lbtrip.domain.user.model.UserTest"`.
- 작업 브랜치: 현재 브랜치 `feat/member-account-api`에서 진행. PR 생성은 사용자가 요청할 때 `scripts/create-pr.sh` 사용.
- 커밋 메시지는 기존 로그 스타일(`feat: 한국어 요약`)을 따르고 아래 트레일러를 붙인다:
  `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`
- `src/main/java/live/lbtrip/domain/recommendation` 패키지는 컨벤션 참고 금지 (단, Task 8에서 해당 패키지의 리포지토리에 조회 메서드를 추가하는 것은 허용 — 기능상 필요).

---

### Task 1: `email_verification_tokens` → `signup_verification_tokens` 리네임

**Files:**
- Create: `src/main/resources/db/migration/V20__rename_email_verification_tokens_to_signup_verification_tokens.sql`
- Rename: `src/main/java/live/lbtrip/domain/auth/model/EmailVerificationToken.java` → `SignupVerificationToken.java`
- Rename: `src/main/java/live/lbtrip/domain/auth/repository/EmailVerificationTokenRepository.java` → `SignupVerificationTokenRepository.java`
- Modify: `src/main/java/live/lbtrip/domain/auth/service/EmailVerificationService.java`, `EmailVerificationCodeGenerator.java`
- Rename: `src/test/java/live/lbtrip/domain/auth/model/EmailVerificationTokenTest.java` → `SignupVerificationTokenTest.java`
- Modify: `src/test/java/live/lbtrip/domain/auth/service/EmailVerificationServiceTest.java`

**Interfaces:**
- Consumes: 없음 (순수 리팩토링)
- Produces: 엔티티 `SignupVerificationToken`(기존 `EmailVerificationToken`과 동일 멤버), `SignupVerificationTokenRepository`(`findByCode`, `existsByCode`). 서비스 클래스명(`EmailVerificationService` 등)과 API 경로는 변경하지 않는다.

- [ ] **Step 1: 마이그레이션 파일 작성**

```sql
RENAME TABLE email_verification_tokens TO signup_verification_tokens;
```

- [ ] **Step 2: 클래스 리네임 및 참조 수정**

`git mv`로 파일 이동 후 클래스명·참조를 일괄 변경한다.

```bash
git mv src/main/java/live/lbtrip/domain/auth/model/EmailVerificationToken.java src/main/java/live/lbtrip/domain/auth/model/SignupVerificationToken.java
git mv src/main/java/live/lbtrip/domain/auth/repository/EmailVerificationTokenRepository.java src/main/java/live/lbtrip/domain/auth/repository/SignupVerificationTokenRepository.java
git mv src/test/java/live/lbtrip/domain/auth/model/EmailVerificationTokenTest.java src/test/java/live/lbtrip/domain/auth/model/SignupVerificationTokenTest.java
```

각 파일에서 `EmailVerificationToken` → `SignupVerificationToken`, `EmailVerificationTokenRepository` → `SignupVerificationTokenRepository`로 치환한다. `SignupVerificationToken`의 `@Table`은 다음과 같이 변경한다:

```java
@Table(name = "signup_verification_tokens")
```

치환 대상 파일: `SignupVerificationToken.java`, `SignupVerificationTokenRepository.java`, `EmailVerificationService.java`, `EmailVerificationCodeGenerator.java`, `SignupVerificationTokenTest.java`, `EmailVerificationServiceTest.java`. `ErrorCode.EMAIL_VERIFICATION_CODE_*`와 DTO·API 경로·서비스 클래스명은 그대로 둔다.

- [ ] **Step 3: 전체 테스트 실행**

Run: `./gradlew test`
Expected: 전체 PASS (동작 변화 없음)

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: 이메일 인증 토큰을 회원가입 인증 토큰으로 리네임

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: 탈퇴 도메인 모델 (`User.withdraw/reinstate/anonymize`, V21)

**Files:**
- Modify: `src/main/java/live/lbtrip/domain/user/model/UserStatus.java`
- Modify: `src/main/java/live/lbtrip/global/error/ErrorCode.java`
- Modify: `src/main/java/live/lbtrip/domain/user/model/User.java`
- Create: `src/main/resources/db/migration/V21__add_user_withdrawal_columns.sql`
- Test: `src/test/java/live/lbtrip/domain/user/model/UserTest.java`

**Interfaces:**
- Consumes: 없음
- Produces: `UserStatus.WITHDRAWN`, `ErrorCode.USER_WITHDRAWN`, `User.withdraw(LocalDateTime now)`, `User.reinstate(LocalDateTime now, Duration gracePeriod)`, `User.anonymize(String encodedPassword, LocalDateTime now)`, `User.isWithdrawn()`, `User.getWithdrawnAt()`, `User.getDeletedAt()`

- [ ] **Step 1: 실패하는 테스트 작성** — `UserTest`에 `@Nested class 회원탈퇴` 추가

```java
@Nested
class 회원탈퇴 {

    @Test
    void 탈퇴하면_상태가_WITHDRAWN이_되고_탈퇴_시점이_기록된다() {
        User user = UserFixture.activeUser();
        LocalDateTime now = LocalDateTime.now();

        user.withdraw(now);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.isWithdrawn()).isTrue();
        assertThat(user.getWithdrawnAt()).isEqualTo(now);
    }

    @Test
    void 이미_탈퇴한_회원이_다시_탈퇴하면_예외가_발생한다() {
        User user = UserFixture.activeUser();
        user.withdraw(LocalDateTime.now());

        assertThatThrownBy(() -> user.withdraw(LocalDateTime.now()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_WITHDRAWN);
    }

    @Test
    void 유예기간_내에_철회하면_ACTIVE로_복원되고_탈퇴_시점이_초기화된다() {
        User user = UserFixture.activeUser();
        user.withdraw(LocalDateTime.now().minusDays(10));

        user.reinstate(LocalDateTime.now(), Duration.ofDays(30));

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getWithdrawnAt()).isNull();
    }

    @Test
    void 유예기간이_지난_회원은_철회할_수_없다() {
        User user = UserFixture.activeUser();
        user.withdraw(LocalDateTime.now().minusDays(40));

        assertThatThrownBy(() -> user.reinstate(LocalDateTime.now(), Duration.ofDays(30)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_WITHDRAWN);
    }

    @Test
    void 탈퇴하지_않은_회원은_철회할_수_없다() {
        User user = UserFixture.activeUser();

        assertThatThrownBy(() -> user.reinstate(LocalDateTime.now(), Duration.ofDays(30)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_WITHDRAWN);
    }

    @Test
    void 익명화하면_식별_정보가_덮어써지고_파기_시점이_기록된다() {
        User user = UserFixture.activeUser();
        user.withdraw(LocalDateTime.now().minusDays(40));
        LocalDateTime now = LocalDateTime.now();

        user.anonymize("anonymized-password", now);

        assertThat(user.getName()).isEqualTo("탈퇴회원");
        assertThat(user.getEmail()).startsWith("withdrawn.").endsWith("@deleted.local");
        assertThat(user.getPassword()).isEqualTo("anonymized-password");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(UserFixture.BIRTH_DATE.getYear(), 1, 1));
        assertThat(user.getDeletedAt()).isEqualTo(now);
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    }

    @Test
    void 익명화된_회원은_철회할_수_없다() {
        User user = UserFixture.activeUser();
        user.withdraw(LocalDateTime.now().minusDays(40));
        user.anonymize("anonymized-password", LocalDateTime.now());

        assertThatThrownBy(() -> user.reinstate(LocalDateTime.now(), Duration.ofDays(365)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_WITHDRAWN);
    }
}
```

필요 import 추가: `java.time.Duration`, `java.time.LocalDate`, `java.time.LocalDateTime`, `live.lbtrip.global.error.BusinessException`, `live.lbtrip.global.error.ErrorCode` (기존 UserTest에 이미 있으면 생략).

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.user.model.UserTest"`
Expected: 컴파일 실패 (`WITHDRAWN`, `withdraw` 심볼 없음)

- [ ] **Step 3: 구현**

`UserStatus`:

```java
public enum UserStatus {
    PENDING_EMAIL_VERIFICATION,
    ACTIVE,
    WITHDRAWN
}
```

`ErrorCode`에 추가 (`EMAIL_NOT_VERIFIED` 아래):

```java
USER_WITHDRAWN(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다."),
```

`User`에 필드 추가 (`marketingAgreed` 아래):

```java
@Column
private LocalDateTime withdrawnAt;

@Column
private LocalDateTime deletedAt;
```

`User`에 메서드 추가 (`isActive()` 근처), import `java.time.Duration`, `java.time.LocalDateTime`:

```java
public void withdraw(LocalDateTime now) {
    if (isWithdrawn()) {
        throw BusinessException.of(ErrorCode.USER_WITHDRAWN);
    }
    this.status = UserStatus.WITHDRAWN;
    this.withdrawnAt = now;
}

public void reinstate(LocalDateTime now, Duration gracePeriod) {
    if (!isWithdrawn() || deletedAt != null || withdrawnAt.plus(gracePeriod).isBefore(now)) {
        throw BusinessException.of(ErrorCode.USER_WITHDRAWN);
    }
    this.status = UserStatus.ACTIVE;
    this.withdrawnAt = null;
}

public void anonymize(String encodedPassword, LocalDateTime now) {
    this.name = "탈퇴회원";
    this.email = "withdrawn." + id + "@deleted.local";
    this.password = encodedPassword;
    this.birthDate = LocalDate.of(birthDate.getYear(), 1, 1);
    this.deletedAt = now;
}

public boolean isWithdrawn() {
    return status == UserStatus.WITHDRAWN;
}
```

V21 마이그레이션:

```sql
ALTER TABLE users
    ADD COLUMN withdrawn_at DATETIME(6) NULL,
    ADD COLUMN deleted_at DATETIME(6) NULL;
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.user.model.UserTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: 회원탈퇴 도메인 모델과 유예기간·익명화 규칙 추가

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 3: 회원탈퇴 API (`DELETE /users/me`)

**Files:**
- Modify: `src/main/java/live/lbtrip/domain/user/service/UserService.java`
- Modify: `src/main/java/live/lbtrip/domain/user/controller/UserApi.java`
- Modify: `src/main/java/live/lbtrip/domain/user/controller/UserController.java`
- Test: `src/test/java/live/lbtrip/domain/user/service/UserServiceTest.java`, `src/test/java/live/lbtrip/domain/user/controller/UserControllerTest.java`

**Interfaces:**
- Consumes: `User.withdraw(LocalDateTime)` (Task 2), `RefreshTokenService.deleteByUserId(Long)` (기존)
- Produces: `UserService.withdraw(Long userId)` (반환 void), `DELETE /users/me` → 200 + `data: null`

- [ ] **Step 1: 실패하는 서비스 테스트 작성** — `UserServiceTest`에 `@Mock RefreshTokenService refreshTokenService;` 추가(`@InjectMocks` 사용 중이면 그대로 주입됨) 후 `@Nested class 회원탈퇴` 추가

```java
@Nested
class 회원탈퇴 {

    @Test
    void 회원을_탈퇴_처리하고_리프레시_토큰을_폐기한다() {
        User user = UserFixture.activeUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.withdraw(1L);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getWithdrawnAt()).isNotNull();
        verify(refreshTokenService).deleteByUserId(1L);
    }

    @Test
    void 존재하지_않는_회원이면_예외가_발생한다() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.withdraw(1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 이미_탈퇴한_회원이면_예외가_발생한다() {
        User user = UserFixture.activeUser();
        user.withdraw(LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.withdraw(1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_WITHDRAWN);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.user.service.UserServiceTest"`
Expected: 컴파일 실패 (`withdraw` 심볼 없음)

- [ ] **Step 3: 서비스 구현** — `UserService`에 `private final RefreshTokenService refreshTokenService;` 필드와 메서드 추가 (import `live.lbtrip.domain.auth.service.RefreshTokenService`, `java.time.LocalDateTime`)

```java
@Transactional
public void withdraw(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));

    user.withdraw(LocalDateTime.now());
    refreshTokenService.deleteByUserId(userId);
}
```

- [ ] **Step 4: 서비스 테스트 통과 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.user.service.UserServiceTest"`
Expected: PASS

- [ ] **Step 5: 실패하는 컨트롤러 테스트 작성** — `UserControllerTest`에 추가 (`delete` static import: `org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete`, `doNothing`/`doThrow`: `org.mockito.Mockito.*`)

```java
@Nested
class 회원탈퇴 {

    @Test
    void 회원탈퇴_요청을_처리한다() throws Exception {
        인증된_사용자();
        doNothing().when(userService).withdraw(AuthResponseFixture.USER_ID);

        mockMvc.perform(delete("/users/me")
                .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"));
    }

    @Test
    void 이미_탈퇴한_회원이면_예외를_응답한다() throws Exception {
        인증된_사용자();
        doThrow(BusinessException.of(ErrorCode.USER_WITHDRAWN))
            .when(userService).withdraw(AuthResponseFixture.USER_ID);

        mockMvc.perform(delete("/users/me")
                .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("USER_WITHDRAWN"));
    }
}
```

- [ ] **Step 6: 컨트롤러 구현**

`UserApi`에 추가:

```java
@SecurityRequirement(name = "bearerAuth")
@Operation(
    summary = "회원탈퇴",
    description = """
        현재 로그인한 사용자를 탈퇴 처리합니다.
        탈퇴 즉시 리프레시 토큰이 폐기되며, 30일의 유예기간 내에 다시 로그인하면 탈퇴가 철회됩니다.
        유예기간이 지나면 개인정보가 파기되어 복구할 수 없습니다.
        """
)
@ApiSuccessResponse(description = "회원탈퇴 성공")
@ApiErrorCodeResponses({INVALID_ACCESS_TOKEN, USER_NOT_FOUND, USER_WITHDRAWN})
ResponseEntity<Void> withdrawUser(@UserId Long userId);
```

`UserController`에 추가 (import `org.springframework.web.bind.annotation.DeleteMapping`):

```java
@DeleteMapping("/me")
public ResponseEntity<Void> withdrawUser(@UserId Long userId) {
    userService.withdraw(userId);
    return ResponseEntity.ok().build();
}
```

- [ ] **Step 7: 컨트롤러 테스트 통과 확인 + 전체 테스트**

Run: `./gradlew test --tests "live.lbtrip.domain.user.controller.UserControllerTest"` 후 `./gradlew test`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: 회원탈퇴 API 추가

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 4: 로그인 시 탈퇴 자동 철회

**Files:**
- Modify: `src/main/java/live/lbtrip/domain/auth/dto/response/LoginResponse.java`
- Modify: `src/main/java/live/lbtrip/domain/auth/service/AuthService.java`
- Modify: `src/main/java/live/lbtrip/domain/auth/controller/AuthApi.java` (login 에러 코드에 `USER_WITHDRAWN` 추가)
- Modify: `src/main/resources/application.yml`, `src/test/resources/application-test.yml`
- Modify: `src/test/java/live/lbtrip/support/fixture/AuthResponseFixture.java`
- Test: `src/test/java/live/lbtrip/domain/auth/service/AuthServiceTest.java`

**Interfaces:**
- Consumes: `User.isWithdrawn()`, `User.reinstate(LocalDateTime, Duration)` (Task 2)
- Produces: `LoginResponse.of(String accessToken, String refreshToken, boolean reinstated)` (3-인자로 변경), 설정 키 `app.withdrawal.grace-period`

- [ ] **Step 1: 실패하는 테스트 작성** — `AuthServiceTest`의 `@InjectMocks`를 제거하고 `@BeforeEach` 수동 생성으로 전환 (`EmailVerificationServiceTest` 패턴), `@Nested class 로그인`(기존 그룹 있으면 그 안에)에 테스트 추가

```java
private static final Duration GRACE_PERIOD = Duration.ofDays(30);

private AuthService authService;

@BeforeEach
void setUp() {
    authService = new AuthService(
        userRepository,
        passwordEncoder,
        emailVerificationService,
        jwtTokenProvider,
        refreshTokenService,
        GRACE_PERIOD
    );
}
```

```java
@Test
void 유예기간_내의_탈퇴_회원이_로그인하면_탈퇴가_철회된다() {
    User user = UserFixture.activeUser();
    user.withdraw(LocalDateTime.now().minusDays(10));
    when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(UserFixture.PASSWORD, UserFixture.ENCODED_PASSWORD)).thenReturn(true);
    when(jwtTokenProvider.createAccessToken(user)).thenReturn(TokenFixture.ACCESS_TOKEN);
    when(refreshTokenService.issue(user)).thenReturn(TokenFixture.REFRESH_TOKEN);

    LoginResponse response = authService.login(AuthRequestFixture.loginRequest());

    assertThat(response.reinstated()).isTrue();
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(user.getWithdrawnAt()).isNull();
}

@Test
void 유예기간이_지난_탈퇴_회원은_로그인할_수_없다() {
    User user = UserFixture.activeUser();
    user.withdraw(LocalDateTime.now().minusDays(40));
    when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(UserFixture.PASSWORD, UserFixture.ENCODED_PASSWORD)).thenReturn(true);

    assertThatThrownBy(() -> authService.login(AuthRequestFixture.loginRequest()))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_WITHDRAWN);
}

@Test
void 일반_회원이_로그인하면_reinstated는_false다() {
    User user = UserFixture.activeUser();
    when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(UserFixture.PASSWORD, UserFixture.ENCODED_PASSWORD)).thenReturn(true);
    when(jwtTokenProvider.createAccessToken(user)).thenReturn(TokenFixture.ACCESS_TOKEN);
    when(refreshTokenService.issue(user)).thenReturn(TokenFixture.REFRESH_TOKEN);

    LoginResponse response = authService.login(AuthRequestFixture.loginRequest());

    assertThat(response.reinstated()).isFalse();
}
```

기존 로그인 성공 테스트가 `LoginResponse.of(a, b)` 2-인자 형태를 검증하면 컴파일 에러가 나므로 함께 수정한다.

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.service.AuthServiceTest"`
Expected: 컴파일 실패 (`reinstated`, 6-인자 생성자 없음)

- [ ] **Step 3: 구현**

`LoginResponse`:

```java
public record LoginResponse(
    @Schema(description = "API 인증에 사용하는 access token", example = "eyJhbGciOiJIUzI1NiJ9.access")
    String accessToken,

    @Schema(description = "access token 갱신에 사용하는 refresh token", example = "eyJhbGciOiJIUzI1NiJ9.refresh")
    String refreshToken,

    @Schema(description = "이번 로그인으로 탈퇴가 철회되었는지 여부", example = "false")
    boolean reinstated
) {
    public static LoginResponse of(String accessToken, String refreshToken, boolean reinstated) {
        return new LoginResponse(accessToken, refreshToken, reinstated);
    }
}
```

`AuthService`: `@RequiredArgsConstructor` 제거, 명시적 생성자로 전환 (import `java.time.Duration`, `java.time.LocalDateTime`, `org.springframework.beans.factory.annotation.Value`):

```java
private final Duration withdrawalGracePeriod;

public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    EmailVerificationService emailVerificationService,
    JwtTokenProvider jwtTokenProvider,
    RefreshTokenService refreshTokenService,
    @Value("${app.withdrawal.grace-period}") Duration withdrawalGracePeriod
) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailVerificationService = emailVerificationService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenService = refreshTokenService;
    this.withdrawalGracePeriod = withdrawalGracePeriod;
}
```

`login` 수정:

```java
@Transactional
public LoginResponse login(LoginRequest request) {
    String email = StringNormalizer.trimToLowerCase(request.email());
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        throw BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }

    boolean reinstated = false;
    if (user.isWithdrawn()) {
        user.reinstate(LocalDateTime.now(), withdrawalGracePeriod);
        reinstated = true;
    }
    if (!user.isActive()) {
        throw BusinessException.of(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    String accessToken = jwtTokenProvider.createAccessToken(user);
    String refreshToken = refreshTokenService.issue(user);

    return LoginResponse.of(accessToken, refreshToken, reinstated);
}
```

`AuthResponseFixture.loginResponse()`:

```java
public static LoginResponse loginResponse() {
    return LoginResponse.of(TokenFixture.ACCESS_TOKEN, TokenFixture.REFRESH_TOKEN, false);
}
```

`AuthApi`의 login `@ApiErrorCodeResponses`에 `USER_WITHDRAWN` 추가, description에 "탈퇴 후 30일 이내에 로그인하면 탈퇴가 철회됩니다." 문장 추가.

`application.yml`의 `app:` 하위에 추가:

```yaml
  withdrawal:
    grace-period: ${WITHDRAWAL_GRACE_PERIOD}
```

`application-test.yml`의 `app:` 하위에 추가:

```yaml
  withdrawal:
    grace-period: 30d
```

- [ ] **Step 4: 테스트 통과 확인 + 전체 테스트**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.service.AuthServiceTest"` 후 `./gradlew test`
Expected: PASS (다른 테스트에서 `LoginResponse` 2-인자 사용처가 있으면 함께 수정)

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: 유예기간 내 로그인 시 회원탈퇴 자동 철회

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 5: `PasswordResetToken` 엔티티 + V22

**Files:**
- Modify: `src/main/java/live/lbtrip/global/error/ErrorCode.java`
- Create: `src/main/java/live/lbtrip/domain/auth/model/PasswordResetToken.java`
- Create: `src/main/java/live/lbtrip/domain/auth/repository/PasswordResetTokenRepository.java`
- Create: `src/main/resources/db/migration/V22__create_password_reset_tokens_table.sql`
- Test: `src/test/java/live/lbtrip/domain/auth/model/PasswordResetTokenTest.java`

**Interfaces:**
- Consumes: `User` 엔티티
- Produces: `PasswordResetToken.create(User user, String code, LocalDateTime codeExpiresAt)`, `issueResetToken(String resetToken, LocalDateTime now, LocalDateTime tokenExpiresAt)`, `use(LocalDateTime now)`, getter `getCode()/getResetToken()/getUser()`; `PasswordResetTokenRepository.findFirstByUserIdAndCodeOrderByIdDesc(Long, String)`, `findByResetToken(String)`, `deleteByUserId(Long)`; `ErrorCode.PASSWORD_RESET_CODE_NOT_FOUND/EXPIRED/USED`, `PASSWORD_RESET_TOKEN_NOT_FOUND/EXPIRED/USED`

- [ ] **Step 1: 실패하는 테스트 작성** — `PasswordResetTokenTest` (`SignupVerificationTokenTest` 스타일)

```java
package live.lbtrip.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.UserFixture;

class PasswordResetTokenTest {

    private static final String CODE = "123456";
    private static final String RESET_TOKEN = "11111111-1111-1111-1111-111111111111";

    @Nested
    class 리셋_토큰_발급 {

        @Test
        void 유효한_코드면_리셋_토큰을_발급한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));

            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));

            assertThat(token.getResetToken()).isEqualTo(RESET_TOKEN);
        }

        @Test
        void 만료된_코드면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.minusMinutes(1));

            assertThatThrownBy(() -> token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }

        @Test
        void 이미_확인된_코드면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));

            assertThatThrownBy(() -> token.issueResetToken("other-token", now, now.plusMinutes(10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_CODE_USED);
        }
    }

    @Nested
    class 리셋_토큰_사용 {

        @Test
        void 유효한_리셋_토큰이면_사용_처리한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));

            token.use(now);

            assertThat(token.isUsed()).isTrue();
        }

        @Test
        void 만료된_리셋_토큰이면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.minusMinutes(1));

            assertThatThrownBy(() -> token.use(now))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        @Test
        void 이미_사용한_리셋_토큰이면_예외가_발생한다() {
            LocalDateTime now = LocalDateTime.now();
            PasswordResetToken token = PasswordResetToken.create(UserFixture.activeUser(), CODE, now.plusMinutes(10));
            token.issueResetToken(RESET_TOKEN, now, now.plusMinutes(10));
            token.use(now);

            assertThatThrownBy(() -> token.use(now))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_USED);
        }
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.model.PasswordResetTokenTest"`
Expected: 컴파일 실패

- [ ] **Step 3: 구현**

`ErrorCode` 추가 (`EMAIL_VERIFICATION_CODE_USED` 아래):

```java
PASSWORD_RESET_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "비밀번호 재설정 인증 코드를 찾을 수 없습니다."),
PASSWORD_RESET_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "비밀번호 재설정 인증 코드가 만료되었습니다."),
PASSWORD_RESET_CODE_USED(HttpStatus.BAD_REQUEST, "이미 사용된 비밀번호 재설정 인증 코드입니다."),
PASSWORD_RESET_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "비밀번호 재설정 토큰을 찾을 수 없습니다."),
PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "비밀번호 재설정 토큰이 만료되었습니다."),
PASSWORD_RESET_TOKEN_USED(HttpStatus.BAD_REQUEST, "이미 사용된 비밀번호 재설정 토큰입니다."),
```

`PasswordResetToken` (`SignupVerificationToken` 패턴):

```java
package live.lbtrip.domain.auth.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "password_reset_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime codeExpiresAt;

    @Column(unique = true, length = 36)
    private String resetToken;

    private LocalDateTime tokenExpiresAt;

    @Column(nullable = false)
    private boolean used;

    private PasswordResetToken(User user, String code, LocalDateTime codeExpiresAt) {
        this.user = user;
        this.code = code;
        this.codeExpiresAt = codeExpiresAt;
        this.used = false;
    }

    public static PasswordResetToken create(User user, String code, LocalDateTime codeExpiresAt) {
        return new PasswordResetToken(user, code, codeExpiresAt);
    }

    public void issueResetToken(String resetToken, LocalDateTime now, LocalDateTime tokenExpiresAt) {
        if (this.resetToken != null) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_USED);
        }
        if (codeExpiresAt.isBefore(now)) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }
        this.resetToken = resetToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public void use(LocalDateTime now) {
        if (used) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_USED);
        }
        if (tokenExpiresAt.isBefore(now)) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
        this.used = true;
    }
}
```

`PasswordResetTokenRepository`:

```java
package live.lbtrip.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.auth.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByUserIdAndCodeOrderByIdDesc(Long userId, String code);

    Optional<PasswordResetToken> findByResetToken(String resetToken);

    void deleteByUserId(Long userId);
}
```

V22 마이그레이션:

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    code VARCHAR(6) NOT NULL,
    code_expires_at DATETIME(6) NOT NULL,
    reset_token VARCHAR(36) NULL,
    token_expires_at DATETIME(6) NULL,
    used BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_reset_token UNIQUE (reset_token),
    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.model.PasswordResetTokenTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: 비밀번호 재설정 토큰 엔티티 추가

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 6: `PasswordResetService` + 코드 생성기 + 재설정 메일

**Files:**
- Create: `src/main/java/live/lbtrip/domain/auth/service/PasswordResetCodeGenerator.java`
- Create: `src/main/java/live/lbtrip/domain/auth/service/PasswordResetMailTemplate.java`
- Create: `src/main/resources/templates/email/password-reset.txt`, `password-reset.html`
- Modify: `src/main/java/live/lbtrip/domain/auth/service/EmailService.java`
- Create: `src/main/java/live/lbtrip/domain/auth/service/PasswordResetService.java`
- Modify: `src/main/resources/application.yml`, `src/test/resources/application-test.yml`
- Create: `src/test/java/live/lbtrip/support/fixture/PasswordResetFixture.java`
- Test: `src/test/java/live/lbtrip/domain/auth/service/PasswordResetServiceTest.java`

**Interfaces:**
- Consumes: `PasswordResetToken`/`PasswordResetTokenRepository` (Task 5), `User.isWithdrawn()` (Task 2), `RefreshTokenService.deleteByUserId`, `UserRepository.findByEmail`
- Produces: `PasswordResetService.request(PasswordResetCodeRequest)` → `PasswordResetCodeResponse`, `confirm(PasswordResetConfirmRequest)` → `PasswordResetTokenResponse`, `reset(PasswordResetRequest)` → void, `EmailService.sendPasswordResetEmail(String toEmail, String code)`. DTO 4종은 Task 7에서 만들지만 서비스가 사용하므로 이 태스크에서 함께 생성한다 (검증 어노테이션 포함, 아래 Step 3 참조).

- [ ] **Step 1: 픽스처와 실패하는 서비스 테스트 작성**

`PasswordResetFixture`:

```java
package live.lbtrip.support.fixture;

public final class PasswordResetFixture {

    public static final String CODE = "654321";
    public static final String RESET_TOKEN = "11111111-1111-1111-1111-111111111111";
    public static final String NEW_PASSWORD = "newpassword1";

    private PasswordResetFixture() {
    }
}
```

`PasswordResetServiceTest` (`EmailVerificationServiceTest` 패턴 — 수동 생성):

```java
package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.model.PasswordResetToken;
import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PasswordResetFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(10);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordResetCodeGenerator codeGenerator;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
            userRepository,
            tokenRepository,
            codeGenerator,
            emailService,
            passwordEncoder,
            refreshTokenService,
            CODE_EXPIRATION,
            TOKEN_EXPIRATION
        );
    }

    @Nested
    class 인증_코드_요청 {

        @Test
        void 인증_코드를_발급하고_메일을_발송한다() {
            User user = UserFixture.activeUser();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(codeGenerator.generate()).thenReturn(PasswordResetFixture.CODE);

            PasswordResetCodeResponse response =
                passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL));

            ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(captor.capture());
            assertThat(captor.getValue().getCode()).isEqualTo(PasswordResetFixture.CODE);
            verify(emailService).sendPasswordResetEmail(UserFixture.EMAIL, PasswordResetFixture.CODE);
            assertThat(response.expiresIn()).isEqualTo(CODE_EXPIRATION.toSeconds());
        }

        @Test
        void 가입되지_않은_이메일이면_예외가_발생한다() {
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 탈퇴한_회원이면_예외가_발생한다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now());
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }

        @Test
        void 이메일_미인증_회원이면_예외가_발생한다() {
            User user = UserFixture.user();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> passwordResetService.request(new PasswordResetCodeRequest(UserFixture.EMAIL)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    @Nested
    class 인증_코드_확인 {

        @Test
        void 코드가_유효하면_리셋_토큰을_발급한다() {
            User user = UserFixture.activeUser();
            PasswordResetToken token =
                PasswordResetToken.create(user, PasswordResetFixture.CODE, LocalDateTime.now().plusMinutes(10));
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(tokenRepository.findFirstByUserIdAndCodeOrderByIdDesc(user.getId(), PasswordResetFixture.CODE))
                .thenReturn(Optional.of(token));

            PasswordResetTokenResponse response = passwordResetService.confirm(
                new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE));

            assertThat(response.resetToken()).isEqualTo(token.getResetToken());
            assertThat(token.getResetToken()).isNotNull();
            assertThat(response.expiresIn()).isEqualTo(TOKEN_EXPIRATION.toSeconds());
        }

        @Test
        void 코드를_찾을_수_없으면_예외가_발생한다() {
            User user = UserFixture.activeUser();
            when(userRepository.findByEmail(UserFixture.EMAIL)).thenReturn(Optional.of(user));
            when(tokenRepository.findFirstByUserIdAndCodeOrderByIdDesc(user.getId(), PasswordResetFixture.CODE))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.confirm(
                    new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_CODE_NOT_FOUND);
        }
    }

    @Nested
    class 비밀번호_재설정 {

        @Test
        void 비밀번호를_변경하고_리프레시_토큰을_폐기한다() {
            User user = UserFixture.activeUser();
            PasswordResetToken token =
                PasswordResetToken.create(user, PasswordResetFixture.CODE, LocalDateTime.now().plusMinutes(10));
            token.issueResetToken(PasswordResetFixture.RESET_TOKEN, LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
            when(tokenRepository.findByResetToken(PasswordResetFixture.RESET_TOKEN)).thenReturn(Optional.of(token));
            when(passwordEncoder.encode(PasswordResetFixture.NEW_PASSWORD)).thenReturn("encoded-new-password");

            passwordResetService.reset(
                new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD));

            assertThat(token.isUsed()).isTrue();
            assertThat(user.getPassword()).isEqualTo("encoded-new-password");
            verify(refreshTokenService).deleteByUserId(user.getId());
        }

        @Test
        void 리셋_토큰을_찾을_수_없으면_예외가_발생한다() {
            when(tokenRepository.findByResetToken(PasswordResetFixture.RESET_TOKEN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.reset(
                    new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND);
        }
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.service.PasswordResetServiceTest"`
Expected: 컴파일 실패

- [ ] **Step 3: 구현**

요청·응답 DTO (`domain/auth/dto/request`, `domain/auth/dto/response`):

```java
package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetCodeRequest(
    @Schema(description = "비밀번호를 재설정할 계정의 이메일", example = "user@example.com", requiredMode = REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    String email
) {
}
```

```java
package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
    @Schema(description = "비밀번호를 재설정할 계정의 이메일", example = "user@example.com", requiredMode = REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    String email,

    @Schema(description = "이메일로 발송된 6자리 인증 코드", example = "123456", requiredMode = REQUIRED)
    @NotBlank(message = "인증 코드는 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
    String code
) {
}
```

```java
package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(
    @Schema(description = "인증 코드 확인 시 발급된 리셋 토큰", example = "11111111-1111-1111-1111-111111111111", requiredMode = REQUIRED)
    @NotBlank(message = "리셋 토큰은 필수입니다.")
    String resetToken,

    @Schema(description = "영문과 숫자를 포함한 8자 이상의 새 비밀번호", example = "newpassword1", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "비밀번호는 영문과 숫자를 포함해 8자 이상이어야 합니다."
    )
    String newPassword
) {
}
```

```java
package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetCodeResponse(
    @Schema(description = "인증 코드 만료까지 남은 시간(초)", example = "600")
    long expiresIn
) {
    public static PasswordResetCodeResponse of(long expiresIn) {
        return new PasswordResetCodeResponse(expiresIn);
    }
}
```

```java
package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetTokenResponse(
    @Schema(description = "새 비밀번호 설정 요청에 사용하는 일회용 리셋 토큰", example = "11111111-1111-1111-1111-111111111111")
    String resetToken,

    @Schema(description = "리셋 토큰 만료까지 남은 시간(초)", example = "600")
    long expiresIn
) {
    public static PasswordResetTokenResponse of(String resetToken, long expiresIn) {
        return new PasswordResetTokenResponse(resetToken, expiresIn);
    }
}
```

`PasswordResetCodeGenerator` (회원가입 생성기와 달리 유일성 DB 조회 없음 — 조회가 `user + code`로 스코프되므로 전역 유일성이 불필요):

```java
package live.lbtrip.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class PasswordResetCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_FORMAT = "%06d";
    private static final int CODE_BOUND = 1_000_000;

    public String generate() {
        return CODE_FORMAT.formatted(RANDOM.nextInt(CODE_BOUND));
    }
}
```

메일 템플릿: `src/main/resources/templates/email/email-verification.txt`와 `email-verification.html`을 각각 `password-reset.txt`, `password-reset.html`로 복사한 뒤, 사용자에게 보이는 문구만 다음 매핑으로 치환한다 (`{{verificationCode}}` 플레이스홀더는 유지):
- "이메일 인증" → "비밀번호 재설정"
- 인증 목적 안내 문장(예: "가입을 완료하려면" 류) → "비밀번호를 재설정하려면 아래 인증번호를 입력해 주세요."

`PasswordResetMailTemplate` (`EmailVerificationMailTemplate`과 동일 구조):

```java
package live.lbtrip.domain.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetMailTemplate {

    private static final String CODE_PLACEHOLDER = "{{verificationCode}}";

    private final String plainTextTemplate;
    private final String htmlTemplate;

    public PasswordResetMailTemplate(
        @Value("classpath:templates/email/password-reset.txt") Resource plainTextTemplateResource,
        @Value("classpath:templates/email/password-reset.html") Resource htmlTemplateResource
    ) {
        try {
            this.plainTextTemplate = plainTextTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            this.htmlTemplate = htmlTemplateResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("비밀번호 재설정 템플릿을 불러올 수 없습니다.", exception);
        }
    }

    public String plainText(String code) {
        return plainTextTemplate.replace(CODE_PLACEHOLDER, code);
    }

    public String html(String code) {
        return htmlTemplate.replace(CODE_PLACEHOLDER, code);
    }
}
```

`EmailService`: 생성자에 `PasswordResetMailTemplate passwordResetMailTemplate` 파라미터·필드 추가, 메서드 추가. 발송 공통부는 private 메서드로 추출한다:

```java
public void sendVerificationEmail(String toEmail, String code) {
    send(toEmail, "[로컬밸런스 트립] 이메일 인증번호를 안내드립니다",
        mailTemplate.plainText(code), mailTemplate.html(code));
}

public void sendPasswordResetEmail(String toEmail, String code) {
    send(toEmail, "[로컬밸런스 트립] 비밀번호 재설정 인증번호를 안내드립니다",
        passwordResetMailTemplate.plainText(code), passwordResetMailTemplate.html(code));
}

private void send(String toEmail, String subject, String plainText, String html) {
    Mail mail = new Mail(
        new Email(from, fromName),
        subject,
        new Email(toEmail),
        new Content("text/plain", plainText)
    );
    mail.addContent(new Content("text/html", html));

    Request request = new Request();
    try {
        request.setMethod(Method.POST);
        request.setEndpoint(MAIL_SEND_ENDPOINT);
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        if (response.getStatusCode() < SUCCESS_STATUS_MIN || response.getStatusCode() > SUCCESS_STATUS_MAX) {
            throw BusinessException.of(ErrorCode.EMAIL_SEND_FAILED);
        }
    } catch (IOException exception) {
        throw BusinessException.of(ErrorCode.EMAIL_SEND_FAILED);
    }
}
```

`PasswordResetService`:

```java
package live.lbtrip.domain.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.model.PasswordResetToken;
import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;

@Service
@Transactional(readOnly = true)
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetCodeGenerator codeGenerator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final Duration codeExpiration;
    private final Duration tokenExpiration;

    public PasswordResetService(
        UserRepository userRepository,
        PasswordResetTokenRepository tokenRepository,
        PasswordResetCodeGenerator codeGenerator,
        EmailService emailService,
        PasswordEncoder passwordEncoder,
        RefreshTokenService refreshTokenService,
        @Value("${app.password-reset.code-expiration}") Duration codeExpiration,
        @Value("${app.password-reset.token-expiration}") Duration tokenExpiration
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.codeExpiration = codeExpiration;
        this.tokenExpiration = tokenExpiration;
    }

    @Transactional
    public PasswordResetCodeResponse request(PasswordResetCodeRequest request) {
        User user = findResettableUser(request.email());

        PasswordResetToken token = PasswordResetToken.create(
            user,
            codeGenerator.generate(),
            LocalDateTime.now().plus(codeExpiration)
        );
        tokenRepository.save(token);
        emailService.sendPasswordResetEmail(user.getEmail(), token.getCode());

        return PasswordResetCodeResponse.of(codeExpiration.toSeconds());
    }

    @Transactional
    public PasswordResetTokenResponse confirm(PasswordResetConfirmRequest request) {
        User user = findResettableUser(request.email());

        PasswordResetToken token = tokenRepository
            .findFirstByUserIdAndCodeOrderByIdDesc(user.getId(), StringNormalizer.trim(request.code()))
            .orElseThrow(() -> BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        token.issueResetToken(UUID.randomUUID().toString(), now, now.plus(tokenExpiration));

        return PasswordResetTokenResponse.of(token.getResetToken(), tokenExpiration.toSeconds());
    }

    @Transactional
    public void reset(PasswordResetRequest request) {
        PasswordResetToken token = tokenRepository.findByResetToken(StringNormalizer.trim(request.resetToken()))
            .orElseThrow(() -> BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND));

        token.use(LocalDateTime.now());

        User user = token.getUser();
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.deleteByUserId(user.getId());
    }

    private User findResettableUser(String email) {
        User user = userRepository.findByEmail(StringNormalizer.trimToLowerCase(email))
            .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));

        if (user.isWithdrawn()) {
            throw BusinessException.of(ErrorCode.USER_WITHDRAWN);
        }
        if (!user.isActive()) {
            throw BusinessException.of(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        return user;
    }
}
```

`application.yml`의 `app:` 하위에 추가:

```yaml
  password-reset:
    code-expiration: ${PASSWORD_RESET_CODE_EXPIRATION}
    token-expiration: ${PASSWORD_RESET_TOKEN_EXPIRATION}
```

`application-test.yml`의 `app:` 하위에 추가:

```yaml
  password-reset:
    code-expiration: 10m
    token-expiration: 10m
```

- [ ] **Step 4: 테스트 통과 확인 + 전체 테스트**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.service.PasswordResetServiceTest"` 후 `./gradlew test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: 비밀번호 재설정 서비스와 인증 메일 발송 추가

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 7: 비밀번호 찾기 API (`PasswordResetController`)

**Files:**
- Create: `src/main/java/live/lbtrip/domain/auth/controller/PasswordResetApi.java`
- Create: `src/main/java/live/lbtrip/domain/auth/controller/PasswordResetController.java`
- Test: `src/test/java/live/lbtrip/domain/auth/controller/PasswordResetControllerTest.java`

**Interfaces:**
- Consumes: `PasswordResetService.request/confirm/reset` + DTO 4종 (Task 6)
- Produces: `POST /auth/password-reset/request`, `POST /auth/password-reset/confirm`, `POST /auth/password-reset`

- [ ] **Step 1: 실패하는 컨트롤러 테스트 작성** (`AuthControllerTest` 패턴 그대로 — `TestCorsConfig`, `@MockitoBean` JwtTokenProvider/AdminJwtTokenProvider/JpaMetamodelMappingContext 포함)

```java
package live.lbtrip.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.auth.service.PasswordResetService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PasswordResetFixture;
import live.lbtrip.support.fixture.UserFixture;

@WebMvcTest(PasswordResetController.class)
@Import(PasswordResetControllerTest.TestCorsConfig.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 인증_코드_요청 {

        @Test
        void 인증_코드_발송_요청을_처리한다() throws Exception {
            when(passwordResetService.request(any(PasswordResetCodeRequest.class)))
                .thenReturn(PasswordResetCodeResponse.of(600L));

            mockMvc.perform(post("/auth/password-reset/request")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new PasswordResetCodeRequest(UserFixture.EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.expiresIn").value(600));
        }

        @Test
        void 이메일_형식이_올바르지_않으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/auth/password-reset/request")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new PasswordResetCodeRequest("invalid-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 가입되지_않은_이메일이면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.USER_NOT_FOUND))
                .when(passwordResetService).request(any(PasswordResetCodeRequest.class));

            mockMvc.perform(post("/auth/password-reset/request")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new PasswordResetCodeRequest(UserFixture.EMAIL))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    class 인증_코드_확인 {

        @Test
        void 인증_코드_확인_요청을_처리한다() throws Exception {
            when(passwordResetService.confirm(any(PasswordResetConfirmRequest.class)))
                .thenReturn(PasswordResetTokenResponse.of(PasswordResetFixture.RESET_TOKEN, 600L));

            mockMvc.perform(post("/auth/password-reset/confirm")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.resetToken").value(PasswordResetFixture.RESET_TOKEN));
        }

        @Test
        void 만료된_코드면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_EXPIRED))
                .when(passwordResetService).confirm(any(PasswordResetConfirmRequest.class));

            mockMvc.perform(post("/auth/password-reset/confirm")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("PASSWORD_RESET_CODE_EXPIRED"));
        }
    }

    @Nested
    class 비밀번호_재설정 {

        @Test
        void 비밀번호_재설정_요청을_처리한다() throws Exception {
            mockMvc.perform(post("/auth/password-reset")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        void 새_비밀번호가_규칙에_맞지_않으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/auth/password-reset")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 이미_사용된_리셋_토큰이면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_USED))
                .when(passwordResetService).reset(any(PasswordResetRequest.class));

            mockMvc.perform(post("/auth/password-reset")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("PASSWORD_RESET_TOKEN_USED"));
        }
    }

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.controller.PasswordResetControllerTest"`
Expected: 컴파일 실패

- [ ] **Step 3: 구현**

`PasswordResetApi`:

```java
package live.lbtrip.domain.auth.controller;

import static live.lbtrip.global.error.ErrorCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;

@Tag(name = "PasswordReset", description = "비밀번호 찾기 API")
public interface PasswordResetApi {

    @Operation(
        summary = "비밀번호 재설정 인증 코드 요청",
        description = """
            가입된 이메일로 6자리 비밀번호 재설정 인증 코드를 발송합니다.
            인증 코드 만료까지 남은 시간(초)을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "인증 코드 발송 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        USER_NOT_FOUND,
        USER_WITHDRAWN,
        EMAIL_NOT_VERIFIED,
        EMAIL_SEND_FAILED
    })
    ResponseEntity<PasswordResetCodeResponse> requestPasswordReset(
        @Valid @RequestBody PasswordResetCodeRequest request
    );

    @Operation(
        summary = "비밀번호 재설정 인증 코드 확인",
        description = """
            이메일로 발송된 6자리 인증 코드를 확인합니다.
            확인에 성공하면 새 비밀번호 설정에 사용할 일회용 리셋 토큰을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "인증 코드 확인 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        USER_NOT_FOUND,
        PASSWORD_RESET_CODE_NOT_FOUND,
        PASSWORD_RESET_CODE_EXPIRED,
        PASSWORD_RESET_CODE_USED
    })
    ResponseEntity<PasswordResetTokenResponse> confirmPasswordReset(
        @Valid @RequestBody PasswordResetConfirmRequest request
    );

    @Operation(
        summary = "새 비밀번호 설정",
        description = """
            리셋 토큰을 검증하고 새 비밀번호로 변경합니다.
            변경에 성공하면 해당 계정의 모든 리프레시 토큰이 폐기됩니다.
            """
    )
    @ApiSuccessResponse(description = "비밀번호 재설정 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        PASSWORD_RESET_TOKEN_NOT_FOUND,
        PASSWORD_RESET_TOKEN_EXPIRED,
        PASSWORD_RESET_TOKEN_USED
    })
    ResponseEntity<Void> resetPassword(
        @Valid @RequestBody PasswordResetRequest request
    );
}
```

`PasswordResetController`:

```java
package live.lbtrip.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController implements PasswordResetApi {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<PasswordResetCodeResponse> requestPasswordReset(
        @Valid @RequestBody PasswordResetCodeRequest request
    ) {
        PasswordResetCodeResponse response = passwordResetService.request(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PasswordResetTokenResponse> confirmPasswordReset(
        @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        PasswordResetTokenResponse response = passwordResetService.confirm(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.reset(request);
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Step 4: 테스트 통과 확인 + 전체 테스트**

Run: `./gradlew test --tests "live.lbtrip.domain.auth.controller.PasswordResetControllerTest"` 후 `./gradlew test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: 비밀번호 찾기 API 추가

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 8: 익명화 스케줄러

**Files:**
- Modify: `src/main/java/live/lbtrip/domain/user/repository/UserRepository.java`
- Modify: `src/main/java/live/lbtrip/domain/savedcourse/model/entity/TourReceipt.java`용 리포지토리 `src/main/java/live/lbtrip/domain/savedcourse/receipt/repository/TourReceiptRepository.java`
- Modify: `src/main/java/live/lbtrip/domain/savedcourse/course/repository/SavedCourseRepository.java`
- Modify: `src/main/java/live/lbtrip/domain/image/repository/ImageRepository.java`
- Modify: `src/main/java/live/lbtrip/domain/recommendation/repository/GeneratedCourseRepository.java`, `RecommendedRegionRepository.java`
- Create: `src/main/java/live/lbtrip/domain/user/service/UserAnonymizationService.java`
- Create: `src/main/java/live/lbtrip/domain/user/scheduler/UserWithdrawalScheduler.java`
- Test: `src/test/java/live/lbtrip/domain/user/service/UserAnonymizationServiceTest.java`

**Interfaces:**
- Consumes: `User.anonymize(String, LocalDateTime)` (Task 2), `SignupVerificationTokenRepository` (Task 1), `PasswordResetTokenRepository.deleteByUserId` (Task 5), `ImageStorage.delete(String key)` (기존), 설정 키 `app.withdrawal.grace-period` (Task 4)
- Produces: `UserAnonymizationService.anonymizeExpiredUsers()` (반환 int: 처리 건수), `UserWithdrawalScheduler` (매일 04시 실행)

- [ ] **Step 1: 리포지토리 조회 메서드 추가**

`UserRepository`:

```java
List<User> findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(UserStatus status, LocalDateTime cutoff);
```

(import `java.time.LocalDateTime`, `java.util.List`, `live.lbtrip.domain.user.model.UserStatus`)

`TourReceiptRepository`:

```java
List<TourReceipt> findAllBySavedCourseUserId(Long userId);
```

`SavedCourseRepository`:

```java
List<SavedCourse> findAllByUserId(Long userId);
```

`ImageRepository`:

```java
List<Image> findAllByUploaderId(Long uploaderId);
```

`GeneratedCourseRepository`:

```java
List<GeneratedCourse> findAllByUserId(Long userId);
```

`RecommendedRegionRepository`에는 기존 `findAllByUserIdOrderByDisplayOrder`가 있으므로 추가하지 않는다.

`SignupVerificationTokenRepository`:

```java
void deleteByUserId(Long userId);
```

- [ ] **Step 2: 실패하는 서비스 테스트 작성**

```java
package live.lbtrip.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.auth.repository.SignupVerificationTokenRepository;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.savedcourse.course.repository.SavedCourseRepository;
import live.lbtrip.domain.savedcourse.receipt.repository.TourReceiptRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserAnonymizationServiceTest {

    private static final Duration GRACE_PERIOD = Duration.ofDays(30);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SignupVerificationTokenRepository signupVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PropensityRepository propensityRepository;

    @Mock
    private RecommendedRegionRepository recommendedRegionRepository;

    @Mock
    private GeneratedCourseRepository generatedCourseRepository;

    @Mock
    private SavedCourseRepository savedCourseRepository;

    @Mock
    private TourReceiptRepository tourReceiptRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageStorage imageStorage;

    private UserAnonymizationService anonymizationService;

    @BeforeEach
    void setUp() {
        anonymizationService = new UserAnonymizationService(
            userRepository,
            passwordEncoder,
            refreshTokenRepository,
            signupVerificationTokenRepository,
            passwordResetTokenRepository,
            propensityRepository,
            recommendedRegionRepository,
            generatedCourseRepository,
            savedCourseRepository,
            tourReceiptRepository,
            imageRepository,
            imageStorage,
            GRACE_PERIOD
        );
    }

    @Nested
    class 만료_회원_익명화 {

        @Test
        void 유예기간이_지난_탈퇴_회원을_익명화하고_연관_데이터를_삭제한다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(40));
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(user));
            when(passwordEncoder.encode(anyString())).thenReturn("anonymized-password");

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isEqualTo(1);
            assertThat(user.getName()).isEqualTo("탈퇴회원");
            assertThat(user.getDeletedAt()).isNotNull();
            verify(tourReceiptRepository).findAllBySavedCourseUserId(user.getId());
            verify(savedCourseRepository).findAllByUserId(user.getId());
            verify(imageRepository).findAllByUploaderId(user.getId());
            verify(generatedCourseRepository).findAllByUserId(user.getId());
            verify(recommendedRegionRepository).findAllByUserIdOrderByDisplayOrder(user.getId());
            verify(propensityRepository).findByUserId(user.getId());
            verify(refreshTokenRepository).deleteByUserId(user.getId());
            verify(signupVerificationTokenRepository).deleteByUserId(user.getId());
            verify(passwordResetTokenRepository).deleteByUserId(user.getId());
        }

        @Test
        void 대상이_없으면_아무것도_처리하지_않는다() {
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isZero();
        }
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `./gradlew test --tests "live.lbtrip.domain.user.service.UserAnonymizationServiceTest"`
Expected: 컴파일 실패

- [ ] **Step 4: 구현**

`UserAnonymizationService` (FK 안전 순서: 투어 영수증 → 저장 코스(장소 cascade) → 이미지 → 생성 코스(장소 cascade) → 추천 지역 → 성향검사 → 토큰류 → 익명화. S3 객체는 DB 삭제 후 best-effort로 제거):

```java
package live.lbtrip.domain.user.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.auth.repository.SignupVerificationTokenRepository;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.savedcourse.course.repository.SavedCourseRepository;
import live.lbtrip.domain.savedcourse.receipt.repository.TourReceiptRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserAnonymizationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SignupVerificationTokenRepository signupVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PropensityRepository propensityRepository;
    private final RecommendedRegionRepository recommendedRegionRepository;
    private final GeneratedCourseRepository generatedCourseRepository;
    private final SavedCourseRepository savedCourseRepository;
    private final TourReceiptRepository tourReceiptRepository;
    private final ImageRepository imageRepository;
    private final ImageStorage imageStorage;
    private final Duration gracePeriod;

    public UserAnonymizationService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        RefreshTokenRepository refreshTokenRepository,
        SignupVerificationTokenRepository signupVerificationTokenRepository,
        PasswordResetTokenRepository passwordResetTokenRepository,
        PropensityRepository propensityRepository,
        RecommendedRegionRepository recommendedRegionRepository,
        GeneratedCourseRepository generatedCourseRepository,
        SavedCourseRepository savedCourseRepository,
        TourReceiptRepository tourReceiptRepository,
        ImageRepository imageRepository,
        ImageStorage imageStorage,
        @Value("${app.withdrawal.grace-period}") Duration gracePeriod
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.signupVerificationTokenRepository = signupVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.propensityRepository = propensityRepository;
        this.recommendedRegionRepository = recommendedRegionRepository;
        this.generatedCourseRepository = generatedCourseRepository;
        this.savedCourseRepository = savedCourseRepository;
        this.tourReceiptRepository = tourReceiptRepository;
        this.imageRepository = imageRepository;
        this.imageStorage = imageStorage;
        this.gracePeriod = gracePeriod;
    }

    @Transactional
    public int anonymizeExpiredUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> targets = userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
            UserStatus.WITHDRAWN, now.minus(gracePeriod));

        for (User user : targets) {
            purgeUserData(user);
            user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()), now);
        }
        return targets.size();
    }

    private void purgeUserData(User user) {
        Long userId = user.getId();

        tourReceiptRepository.deleteAll(tourReceiptRepository.findAllBySavedCourseUserId(userId));
        savedCourseRepository.deleteAll(savedCourseRepository.findAllByUserId(userId));

        List<Image> images = imageRepository.findAllByUploaderId(userId);
        List<String> storageKeys = images.stream().map(Image::getStorageKey).toList();
        imageRepository.deleteAll(images);

        generatedCourseRepository.deleteAll(generatedCourseRepository.findAllByUserId(userId));
        recommendedRegionRepository.deleteAll(
            recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId));
        propensityRepository.findByUserId(userId).ifPresent(propensityRepository::delete);

        refreshTokenRepository.deleteByUserId(userId);
        signupVerificationTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);

        deleteStorageObjects(storageKeys);
    }

    private void deleteStorageObjects(List<String> storageKeys) {
        for (String key : storageKeys) {
            try {
                imageStorage.delete(key);
            } catch (RuntimeException exception) {
                log.warn("탈퇴 회원 이미지 삭제 실패. key={}", key, exception);
            }
        }
    }
}
```

`UserWithdrawalScheduler`:

```java
package live.lbtrip.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.user.service.UserAnonymizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawalScheduler {

    private final UserAnonymizationService anonymizationService;

    @Scheduled(cron = "0 0 4 * * *")
    public void anonymizeExpiredUsers() {
        int count = anonymizationService.anonymizeExpiredUsers();
        log.info("탈퇴 유예기간 만료 회원 익명화 완료. count={}", count);
    }
}
```

- [ ] **Step 5: 테스트 통과 확인 + 전체 테스트**

Run: `./gradlew test --tests "live.lbtrip.domain.user.service.UserAnonymizationServiceTest"` 후 `./gradlew test`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: 탈퇴 유예기간 만료 회원 익명화 스케줄러 추가

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

## 스펙 커버리지 점검표

| 스펙 항목 | 태스크 |
|---|---|
| 테이블 리네임 (V20) | Task 1 |
| `users` 컬럼 추가 (V21), WITHDRAWN 상태, 탈퇴·철회·익명화 도메인 규칙 | Task 2 |
| `DELETE /users/me` + 리프레시 토큰 폐기 | Task 3 |
| 로그인 자동 철회 + `reinstated` 응답 + 유예기간 설정값 | Task 4 |
| `password_reset_tokens` (V22) + 상태 전이 + 신규 ErrorCode | Task 5 |
| request/confirm/reset 서비스 로직 + 재설정 메일 + 만료 설정값 | Task 6 |
| 비밀번호 찾기 API 3개 + Swagger | Task 7 |
| 익명화 스케줄러 + 연관 데이터 삭제 | Task 8 |
| 탈퇴 회원 비밀번호 찾기 차단 | Task 6 (`findResettableUser`) |
| 유예기간 중 재가입 차단 | 기존 `users.email` 유니크 제약으로 충족 (코드 변경 없음) |
