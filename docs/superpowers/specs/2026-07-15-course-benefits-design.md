# 코스 상세 응답 "적용 가능 혜택" 추가 설계

- 날짜: 2026-07-15
- 브랜치: `feat/19-integrate-course-recommendation-benefits`
- 대상 API: `GET /recommendations/courses/{courseId}`
- 디자인: Figma `웹 코스 추천 · 03 코스 상세` (node 553-2) — "이 코스 적용 가능 혜택" 섹션

## 배경

코스 상세 화면에는 "이 코스 적용 가능 혜택" 섹션이 있으며, 행마다 혜택명 + 부가 설명 + 외부 링크로
구성된다. 현재 `Incentive`는 `title`, `url`만 가지며 코스와 매칭할 기준이 없다.

## 결정 사항

1. **매칭 기준: 지역(시군구) 기반.** 혜택에 적용 지역 목록을 부여하고, 코스의 추천 지역과 매칭한다.
2. **스코프 모델: 매핑 테이블 기반 다중 지역.** 전국 적용 플래그는 두지 않는다. 여러 지역에 적용되는
   혜택은 매핑 행을 여러 개 등록한다.
3. **매칭 키: 법정동 코드(`ldong_regn_cd` 시도 2자리 + `ldong_signgu_cd` 시군구 3자리).**
   `region_candidates`가 이미 이 코드를 자연키(유니크 제약)로 사용 중이며, 표준 코드라 시드 재적재에도
   안정적이다. 도메인 간 FK 결합(대안 B)과 지역명 문자열 조인(대안 C)은 기각.

## 데이터 모델 / 마이그레이션

### V10 — `recommended_regions`에 지역 코드 복원

- `ldong_regn_cd VARCHAR(2)` NULL, `ldong_signgu_cd VARCHAR(3)` NULL 컬럼 추가.
- 기존 행은 `region_candidates.name = recommended_regions.region_name` 조인으로 백필.
- 백필 실패 행은 null 유지 → 해당 코스는 혜택 빈 배열로 응답하며, 추천 재생성 시 자연 복구된다.

### V11 — 혜택 확장

- `incentives.description VARCHAR(200)` NULL 컬럼 추가 (피그마 부가 설명 대응).
- `incentive_regions` 테이블 신설:

```sql
CREATE TABLE incentive_regions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    incentive_id BIGINT NOT NULL,
    ldong_regn_cd VARCHAR(2) NOT NULL,
    ldong_signgu_cd VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_incentive_regions UNIQUE (incentive_id, ldong_regn_cd, ldong_signgu_cd),
    CONSTRAINT fk_incentive_regions_incentive FOREIGN KEY (incentive_id)
        REFERENCES incentives (id) ON DELETE CASCADE
);
```

## 도메인 변경

- `IncentiveRegion` 엔티티 신설(`domain/admin/incentive/model`):
  `@ManyToOne(LAZY) Incentive` + 법정동 코드 2개 컬럼, `create(...)` 정적 팩토리.
- `Incentive`: `description` 필드, `@OneToMany(cascade = ALL, orphanRemoval = true)` regions 컬렉션,
  `replaceRegions(...)` 도메인 메서드 추가. `create`/`update` 시그니처에 `description` 반영.
- `RecommendedRegion`: `ldongRegnCd`, `ldongSignguCd` 필드 추가, `create(...)` 시그니처 확장.
- `RegionPlan`: 법정동 코드 2개 필드 추가. `RecommendationGenerationService`가 `RegionStats`의 코드를
  전달하고 `RecommendationStore`가 저장한다(생성 파이프라인에 이미 흐르는 값을 끝까지 운반).

## 어드민 CRUD 변경

- `IncentiveRequest`: `description`(선택, 최대 200자) + `regions` 목록 추가.
  각 항목은 `ldongRegnCd`(2자리), `ldongSignguCd`(3자리) — Bean Validation 한국어 메시지 포함.
- 수정 시 지역 목록은 전체 교체(replace) 방식.
- 등록/수정 시 각 코드 쌍이 `region_candidates`에 존재하는지 검증.
  실패 시 `ErrorCode.INCENTIVE_REGION_INVALID(HttpStatus.BAD_REQUEST, "존재하지 않는 지역 코드입니다.")`.
- `IncentiveResponse`: `description`, `regions` 노출.
- Swagger 문서는 `IncentiveApi` 인터페이스에서 갱신.

## 코스 상세 API 응답

`CourseDetailResponse`에 `benefits` 필드 추가:

```json
"benefits": [
  { "title": "KTX 인구감소지역 할인", "description": "코레일 공식 채널로 이동", "url": "https://..." }
]
```

- 조회 흐름: 코스 → `recommendedRegion`의 법정동 코드 →
  `IncentiveRepository`의 코드 매칭 조인 쿼리(`incentive_regions` join, `incentive.id` 오름차순, 중복 제거)
  → `InnerBenefitResponse`(`title`, `description`, `url`) 변환.
- 지역 코드가 null인 레거시 추천 지역은 빈 배열을 반환한다.
- `RecommendationService`가 `IncentiveRepository`를 참조한다
  (기존 `UserRepository` 크로스 도메인 참조와 동일한 패턴).
- `RecommendationApi`의 Swagger 문서 갱신.

## 테스트

사용자 지침(테스트 코드 작성 금지)에 따라 신규 테스트는 작성하지 않는다.
기존 incentive 테스트가 시그니처 변경으로 컴파일이 깨지면 컴파일만 통과하도록 최소 수정한다.

## 범위 제외 (YAGNI)

- 전국 적용 플래그, 혜택 노출 기간/정렬 우선순위, 사용자 조건(나이 등) 매칭.
- 어드민용 지역 후보 목록 조회 API(코드 참조용) — 필요해지면 별도 작업.
