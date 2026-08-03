# 비밀번호 찾기 · 회원탈퇴 설계

- 작성일: 2026-08-03
- 관련 Figma: 회원가입·로그인(node 509-3), 마이페이지(node 1038-10)

## 1. 개요

두 기능을 추가한다.

1. **비밀번호 찾기**: 이메일 인증 코드 확인 후 비밀번호를 재설정한다.
2. **회원탈퇴**: 소프트 삭제 + 30일 유예기간 방식. 유예기간 중 로그인하면 탈퇴가 자동 철회되고, 유예기간이 지나면 스케줄러가 개인정보를 익명화하고 연관 데이터를 삭제한다.

## 2. 확정된 정책 결정

| 항목 | 결정 |
|---|---|
| 탈퇴 처리 방식 | 소프트 삭제 + 유예기간 |
| 유예기간 | 30일 (`application.yml` 설정값) |
| 탈퇴 철회 | 유예기간 중 로그인 성공 시 자동 철회 |
| 유예기간 만료 처리 | 익명화 + 연관 데이터 삭제 (일 1회 스케줄러) |
| 미가입 이메일로 비밀번호 찾기 요청 | `USER_NOT_FOUND` 에러 응답 (user enumeration 보호는 기존 이메일 중복확인 API가 이미 가입 여부를 노출하므로 실익 없음) |
| 재설정 인가 방식 | 코드 확인 성공 시 단기 일회용 resetToken(UUID) 발급 |
| 토큰 테이블 | 회원가입용과 비밀번호 재설정용 완전 분리. 기존 테이블은 `signup_verification_tokens`로 리네임 |
| 파기 완료 표시 | `users.deleted_at` (status enum에 DELETED 추가하지 않음) |
| 전역 소프트 삭제 | 이번 범위에서 제외. 후속 작업으로 분리하되 `users.deleted_at` 설계는 추후 `BaseEntity` 승격과 호환되게 유지 |

## 3. 비밀번호 찾기

### 3.1 API

모두 비인증 API이며 `domain/auth`에 위치한다. `PasswordResetApi` 인터페이스 + `PasswordResetController` 구조를 따른다.

| 메서드 | 경로 | 요청 | 성공 응답 |
|---|---|---|---|
| POST | `/auth/password-reset/request` | `{ email }` | 코드 유효시간(초) |
| POST | `/auth/password-reset/confirm` | `{ email, code }` | `{ resetToken, expiresIn }` |
| POST | `/auth/password-reset` | `{ resetToken, newPassword }` | 성공 여부 |

### 3.2 동작 규칙

**request**
- 이메일은 `StringNormalizer.trimToLowerCase` 정규화 후 조회한다.
- 미가입: `USER_NOT_FOUND`. 탈퇴 상태: `USER_WITHDRAWN`. 이메일 미인증(PENDING_EMAIL_VERIFICATION): `EMAIL_NOT_VERIFIED`.
- ACTIVE 회원이면 `password_reset_tokens` 행을 생성(6자리 코드, 만료시간)하고 재설정용 메일 템플릿으로 발송한다.
- 재요청 시 새 행을 생성한다. 이전 행은 만료시간까지 유효하다 (기존 회원가입 재전송과 동일 정책).

**confirm**
- `email + code`로 조회한다 (코드 단독 조회 금지 — 사용자 간 코드 충돌 방지).
- 코드 검증 실패: `PASSWORD_RESET_CODE_NOT_FOUND` / 만료: `PASSWORD_RESET_CODE_EXPIRED` / 이미 확인됨: `PASSWORD_RESET_CODE_USED`.
- 성공 시 같은 행에 `reset_token`(UUID)과 `token_expires_at`(발급 시점 + 10분)을 기록한다. `reset_token IS NOT NULL`이 코드 소모 상태를 의미한다.

**reset**
- `reset_token`으로 조회. 실패: `PASSWORD_RESET_TOKEN_NOT_FOUND` / 만료: `PASSWORD_RESET_TOKEN_EXPIRED` / 사용됨: `PASSWORD_RESET_TOKEN_USED`.
- 새 비밀번호는 회원가입과 동일한 Bean Validation 규칙을 적용한다.
- `user.changePassword(인코딩된 비밀번호)` 호출 후 `used = true` 처리한다.
- 해당 유저의 리프레시 토큰을 전부 폐기한다 (세션 강제 만료).

### 3.3 엔티티: `PasswordResetToken`

한 행이 재설정 시도 1회의 전체 생명주기를 표현한다.

```
password_reset_tokens
├─ id (PK)
├─ user_id (FK, NOT NULL)
├─ code VARCHAR(6) NOT NULL            -- request 시 생성
├─ code_expires_at DATETIME NOT NULL
├─ reset_token VARCHAR(36) NULL UNIQUE -- confirm 성공 시 채움
├─ token_expires_at DATETIME NULL
├─ used BOOLEAN NOT NULL DEFAULT FALSE -- reset 완료 시 true
├─ created_at / updated_at (BaseEntity)
```

상태 전이(`발급됨 → 코드확인됨 → 사용됨`)와 검증은 엔티티 메서드로 구현하고, 위반 시 `BusinessException.of(ErrorCode.X)`를 던진다.

### 3.4 설정

`application.yml`의 `app.password-reset` 하위: `code-expiration`(기존 회원가입 코드와 동일 기본값), `token-expiration`(10분).

## 4. 회원탈퇴

### 4.1 API

| 메서드 | 경로 | 인증 | 동작 |
|---|---|---|---|
| DELETE | `/users/me` | bearerAuth + `@UserId` | 탈퇴 처리 |

- Figma 확인 모달대로 비밀번호 재입력 없이 진행한다.
- `status = WITHDRAWN`, `withdrawn_at = now()` 기록 후 해당 유저의 리프레시 토큰을 전부 폐기한다.
- 이미 WITHDRAWN인 유저의 재요청: `USER_WITHDRAWN`.

### 4.2 로그인 시 자동 철회

`AuthService.login` 흐름에 추가한다.

- `WITHDRAWN` && `deleted_at IS NULL` && `withdrawn_at + 30일 > now`: 비밀번호 검증 통과 시 `status = ACTIVE`, `withdrawn_at = null`로 복원하고 정상 로그인. 응답에 `reinstated: true`를 포함해 프론트가 철회 안내를 띄울 수 있게 한다 (평상시 로그인은 `reinstated: false`).
- 그 외 WITHDRAWN 상태(유예기간 경과 또는 익명화 완료): `USER_WITHDRAWN`.

### 4.3 파생 규칙

- 유예기간 중 동일 이메일 재가입은 `users.email` 유니크 제약으로 차단된다 (탈퇴→재가입 어뷰징 방지). 익명화 후에는 이메일이 해제되어 재가입 가능하다.
- 탈퇴 상태 유저의 비밀번호 찾기 요청은 `USER_WITHDRAWN`으로 차단한다.

### 4.4 익명화 스케줄러

매일 1회 실행하는 신규 `@Scheduled` 컴포넌트 (기존 주석 처리된 스케줄러와 무관하게 새로 추가).

- 대상: `status = WITHDRAWN AND withdrawn_at <= now - 유예기간 AND deleted_at IS NULL`
- 처리:
  - `name` → `탈퇴회원`
  - `email` → `withdrawn.{id}@deleted.local` (유니크 제약 유지)
  - `password` → 랜덤 값의 BCrypt 해시 (로그인 불가)
  - `birth_date` → 같은 해의 1월 1일 (연령대 통계 유지, 식별성 제거)
  - `gender`, 약관 동의 값은 유지 (비식별 통계용)
  - 연관 데이터 삭제: 성향검사 결과, 저장한 코스, 회원가입 인증 토큰, 비밀번호 재설정 토큰, 리프레시 토큰
  - `deleted_at = now()` 기록 (재처리 방지 마커)
- 익명화 로직은 `User` 엔티티 메서드(`anonymize(...)`)로 구현한다.

## 5. 스키마 변경 (Flyway)

| 파일 | 내용 |
|---|---|
| `V20__rename_email_verification_tokens_to_signup_verification_tokens.sql` | 테이블 리네임. 엔티티도 `SignupVerificationToken`으로 리네임 |
| `V21__add_user_withdrawal_columns.sql` | `users`에 `withdrawn_at DATETIME NULL`, `deleted_at DATETIME NULL` 추가 |
| `V22__create_password_reset_tokens_table.sql` | 3.3 스키마로 테이블 생성 |

`UserStatus`는 `PENDING_EMAIL_VERIFICATION / ACTIVE / WITHDRAWN` 3개 값이 된다 (status 컬럼은 VARCHAR라 마이그레이션 불필요).

## 6. 신규 ErrorCode

HTTP 상태와 한국어 메시지를 포함해 추가한다.

| 코드 | 상태 |
|---|---|
| `USER_WITHDRAWN` | 403 |
| `EMAIL_NOT_VERIFIED` | 403 |
| `PASSWORD_RESET_CODE_NOT_FOUND` | 404 |
| `PASSWORD_RESET_CODE_EXPIRED` | 400 |
| `PASSWORD_RESET_CODE_USED` | 400 |
| `PASSWORD_RESET_TOKEN_NOT_FOUND` | 404 |
| `PASSWORD_RESET_TOKEN_EXPIRED` | 400 |
| `PASSWORD_RESET_TOKEN_USED` | 400 |

기존 코드에 동일 의미가 이미 있으면 재사용하고 신규 추가를 생략한다.

## 7. 테스트

- 컨트롤러: `@WebMvcTest` + MockMvc, `$.result` / `$.data` / `$.error.code` 검증.
- 서비스: `@ExtendWith(MockitoExtension.class)` 단위 테스트. 비즈니스 예외는 타입 + `errorCode` 모두 검증.
- 엔티티: `PasswordResetToken` 상태 전이, `User.anonymize` / 탈퇴·철회 메서드 단위 테스트.
- 픽스처: `support/fixture` 하위에 추가. 한국어 테스트명 + `@Nested` 그룹핑.

## 8. 범위 제외 (후속 작업)

- 전역 소프트 삭제 도입: `BaseEntity.deleted_at` 승격, 엔티티별 `@SQLDelete`/`@SQLRestriction`, 테이블별 유니크 제약 재설계, 소프트/하드 삭제 대상 분류. 별도 이슈·PR로 진행한다.
