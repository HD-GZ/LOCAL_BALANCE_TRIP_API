# POST /recommendations 추천 파이프라인 설계

- 작성일: 2026-07-13
- 브랜치: `feat/implement-recommendation-api`
- 대상 API: `POST /recommendations` (RecommendationController.createRecommendations)

## 1. 배경과 목표

로컬밸런스 트립의 핵심 MVP는 "사용자 10축 성향 → 인구감소지역 중심 추천 지역 최대 5곳 → 지역별 코스 최대 3개 → 코스별 장소 스냅샷" 생성이다.
5곳·3개는 상한(리미트)이며, 조건에 맞는 결과가 그보다 적으면 적은 대로 저장한다.
조회 API 4개(여행지 리스트 / 코스 리스트 / 코스 상세 / 코스 저장)는 이미 저장된 스냅샷을 읽기만 하므로,
이 문서는 유일한 쓰기 경로인 `POST /recommendations`의 생성 파이프라인을 정의한다.

### 확정된 결정 (브레인스토밍 Q&A)

| 결정 항목 | 선택 |
|---|---|
| LLM 역할 범위 | **하이브리드** — 지역 선정은 규칙 기반, 코스 구성·추천 이유 문구는 LLM |
| 지역 특성 데이터 | **TourAPI 실시간 집계** (정적 시드 없음) |
| 후보 지역 풀 | **행안부 지정 인구감소지역 89곳 전체** |
| LLM 스택 | **Spring AI + OpenAI** (gpt-4o-mini 급) |
| 오디오가이드(Odii) | **이번 MVP에 포함** (TourAPI·Odii 모두 활용신청 완료) |
| 지역 5곳·코스 3개 | **최대 개수(리미트)** — 그보다 적어도 됨 |
| 후보 지역 관리 | **DB 테이블 + Flyway 시드** (enum 아님) |
| 테스트 코드 | **작성하지 않음** — 수동 검증으로 대체 |
| sociality·transportation | **지역 스코어링에서 제외** (LLM 프롬프트에는 전달) |

### 제약 (사용자 요구사항)

- TourAPI 원본 응답을 DB에 저장하지 않는다. 기존 스냅샷 테이블(`recommended_regions`, `generated_courses`, `course_places`)에 필요한 필드만 저장한다.
- 캐싱, 병렬 처리, 재시도/서킷브레이커 등 오버엔지니어링을 하지 않는다. 모든 외부 호출은 순차 실행한다.
- API 명세에 "수십 초 소요"가 이미 문서화되어 있으므로 긴 응답 시간은 계약 범위 내다.

## 2. 파이프라인 개요

```
① 성향 로드        Propensity 조회 (없으면 PROPENSITY_NOT_FOUND)
② 지역 스코어링     후보 지역(DB) × areaBasedList2 1회 → 시그널 집계 → 성향 매칭 점수 → 상위 최대 5곳
③ 장소 후보 수집    선정 지역 × contentType별 목록 조회 (12·14·28·38·39)
④ LLM 코스 구성    지역당 1회 ChatClient 호출 → 지역 reason + 코스 최대 3개(이름·reason·장소 contentId 순서)
⑤ 장소 상세 보강    LLM이 선택한 장소만 detailCommon2로 overview 조회
⑥ 오디오 매칭      지역당 Odii 위치기반 1회 조회 → 좌표 근접 + 제목 매칭
⑦ 도보 시간 계산    인접 장소 간 하버사인 거리 ÷ 도보 속도(4km/h)
⑧ 저장(트랜잭션)    기존 추천 삭제 → recommended_regions / generated_courses / course_places 재생성
```

핵심 원칙: **LLM은 후보 집합 안에서 선택과 문구 창작만 한다.** 장소의 사실 정보(이름, overview,
이미지, 좌표)는 전부 TourAPI 원본에서 채우고, LLM이 후보에 없는 contentId를 반환하면 그 장소는 버린다.
LLM의 환각이 DB에 들어갈 경로가 없다.

호출 예산(순차): 지역 스코어링 89회 + 후보 수집 25회(5지역×5타입) + LLM 5회 + 상세 보강 최대 75회
(5지역×3코스×최대 5장소) + Odii 지역당 1회 + 매칭된 장소의 스토리 조회 소량 ≈ **약 200회, 40~80초 예상**.

## 3. 단계별 상세

### ① 성향 로드

- `PropensityRepository.findByUserId(userId)` → 없으면 `BusinessException.of(PROPENSITY_NOT_FOUND)`.
- 10축 원점수(1~5)를 사용한다: Preference(locality, frugality, experientiality, vitality, sociality) +
  ValueConsumption(accommodation, food, experience, transportation, cafeExhibition).

### ② 지역 스코어링 (규칙 기반)

**후보 풀**: 인구감소지역 89곳을 **DB 테이블 `region_candidates`로 관리**한다.
컬럼은 표시용 지역명(예: "전라북도 임실군"), TourAPI `area_code`, `sigungu_code` + BaseEntity 공통 컬럼이다.
특성 점수는 갖지 않는다(실시간 집계 방식이므로). 89곳 데이터는 Flyway 시드 마이그레이션
(`V9__create_region_candidates_table.sql`, 테이블 생성 + INSERT)으로 넣고, 파이프라인은 `findAll()`로 읽는다.

**시그널 수집**: 지역당 `areaBasedList2`를 contentTypeId 없이 `numOfRows=100`으로 1회 호출한다.

- `totalCount` = 지역 전체 관광 콘텐츠 규모 → 적을수록 "한적한 로컬" 시그널
- 표본 100건의 `contenttypeid` 분포 → 타입별 비중(ratio)

**정규화 공식**: `norm(x) = (x − min) / (max − min)` — 후보 지역 전체에서 min-max 정규화해 0~1로 만든다.
locality 시그널은 `rarity = 1 − norm(totalCount)`인데, 관광 콘텐츠가 **적을수록** 한적한 로컬이므로
정규화 값을 뒤집어(1에서 빼서) 콘텐츠 희소 지역이 1에 가깝도록 한 것이다.
max = min(모든 지역이 동일)이면 해당 시그널은 전 지역 0으로 처리한다.

**시그널 → 성향 축 매핑** (기본안, 구현 중 튜닝 가능):

| 시그널 (89곳 기준 min-max 정규화) | 매칭 축 |
|---|---|
| rarity = 1 − norm(totalCount) | locality |
| 쇼핑(38) 비중 — 전통시장 포함 | frugality |
| 문화시설(14) 비중 | experientiality, cafeExhibition |
| 레포츠(28) 비중 | vitality, experience |
| 음식점(39) 비중 | food |
| 숙박(32) 비중 (표본 내 등장 시) | accommodation |
| (시그널 없음 — 스코어링 제외) | sociality, transportation |

**점수 공식**:

```
축 가중치 w = 사용자 원점수 − 3          // 1~5점 → -2~+2
regionScore = Σ (w_axis × normalizedSignal_axis)
```

점수 상위 최대 5곳을 선정하고 `display_order`는 점수 순위로 부여한다.
sociality·transportation은 TourAPI 목록 데이터에서 대응 시그널이 없어 지역 스코어링에서는 제외하되,
④의 LLM 프롬프트에는 10축 전부를 전달해 코스 구성(동선 강도, 동행 적합성 등)에 반영한다.

### ③ 장소 후보 수집

선정된 지역 각각에 대해 `areaBasedList2`를 contentType별(관광지 12, 문화시설 14, 레포츠 28,
쇼핑 38, 음식점 39)로 호출한다. `numOfRows=15`, `arrange=O`(대표 이미지가 있는 항목만, 제목순).
후보당 보관 필드: `contentId`, `title`, `contentTypeId`, `firstimage`, `mapx`, `mapy`.
지역당 최대 75곳의 후보 풀이 만들어진다.

### ④ LLM 코스 구성 (Spring AI + OpenAI)

지역당 1회, `ChatClient` + 구조화 출력(`entity()`)으로 호출한다.

**입력**: 사용자 10축 성향 요약(축 이름·점수·의미), 지역명, 후보 장소 목록(contentId, 제목, 타입, 좌표).

**출력 스키마**:

```json
{
  "regionReason": "지역 추천 이유 (300자 이내, recommended_regions.reason)",
  "courses": [
    {
      "name": "코스 이름 (100자 이내)",
      "reason": "코스 추천 이유 (300자 이내)",
      "placeContentIds": ["후보 목록 안의 contentId만, 방문 순서대로 3~5개"]
    }
  ]
}
```

**프롬프트 요구사항**:
- 코스는 최대 3개(가능하면 3개), 서로 다른 테마(예: 미식/힐링·산책/체험)로 구성하고 사용자 성향 상위 축을 테마에 반영할 것
- 장소는 반드시 후보 목록의 contentId에서만 선택하고, 도보 이동을 고려해 가까운 장소끼리 묶을 것
- reason은 기존 더미 데이터 톤("~을 반영해 ~로 짰어요")의 자연스러운 한국어 문장으로 작성할 것

**검증 (얇게)**:
- 후보에 없는 contentId → 해당 장소만 제거
- 제거 후 장소가 2개 미만인 코스 → 해당 코스 제거
- 코스가 3개를 초과하면 앞에서 3개만 사용, 남은 코스가 0개면 `RECOMMENDATION_GENERATION_FAILED`
- 길이 초과 문구는 저장 전 잘라낸다 (컬럼 제약: reason 300자, name 100자)

### ⑤ 장소 상세 보강

LLM이 선택한 장소(최대 75곳)만 `detailCommon2`로 `overview`를 조회한다.
후보 전체(~375곳)를 보강하지 않고 최종 선택 이후로 미뤄 호출 수를 줄인다.
이미지·좌표는 ③에서 이미 확보했으므로 추가 조회 없음.

- 코스 대표 이미지 = 첫 번째 장소의 `firstimage`
- 지역 대표 이미지 = 해당 지역 1번 코스의 대표 이미지

### ⑥ 오디오 매칭 (Odii)

- 지역당 1회 Odii `themeLocationBasedList`(지역 후보 장소들의 좌표 평균 중심, 반경 최대치)를 호출한다.
- 각 코스 장소에 대해 **좌표 500m 이내 + 정규화된 제목 일치(공백 제거 후 포함 관계)**인 테마를 찾는다.
- 매칭된 테마만 `storySearchList`(또는 storyBasedList)로 첫 스토리의 `audioUrl`을 조회한다.
- 매칭 성공 시 `has_audio=true, audio_url=...`, 실패 시 `has_audio=false, audio_url=null`.
- Odii 호출 실패는 추천 전체를 실패시키지 않는다 — 오디오 없이 진행한다 (부가 기능이므로).

### ⑦ 도보 시간 계산

`walk_minutes` = 이전 장소와의 하버사인 거리(m) ÷ 67(m/분, 4km/h), 반올림. 첫 장소는 null.
순수 함수로 구현한다.

### ⑧ 저장 (트랜잭션 경계)

**외부 호출(②~⑥)은 트랜잭션 밖에서 수행하고, 결과가 모두 메모리에 준비된 뒤 저장만 트랜잭션으로 묶는다.**
수십 초짜리 외부 호출이 DB 커넥션과 트랜잭션을 점유하지 않도록 한다.

- 덮어쓰기: 해당 사용자의 `course_places` → `generated_courses` → `recommended_regions` 순으로 삭제 후 재생성
- `saved_courses`는 독립 스냅샷(FK 없음)이므로 건드리지 않는다 — 재추천해도 저장 코스는 유지

## 4. 컴포넌트 구조

```
domain/recommendation/
  client/
    TourApiClient          # areaBasedList2, detailCommon2 (RestClient)
    OdiiClient             # themeLocationBasedList, storySearchList (RestClient)
    dto/                   # TourAPI/Odii 응답 파싱용 record (DB 저장 안 함)
  service/
    RecommendationService  # 오케스트레이터. 저장 단계만 @Transactional
    RegionScorer           # ② 순수 로직 (시그널 정규화 + 점수 계산)
    CourseComposer         # ④ Spring AI ChatClient 래퍼 + 응답 검증
    WalkTimeCalculator     # ⑦ 순수 함수 (static 유틸 또는 컴포넌트)
  model/entity/
    RegionCandidate        # 인구감소지역 후보 엔티티 (지역명, areaCode, sigunguCode) — Flyway 시드로 적재
  repository/
    RegionCandidateRepository, RecommendedRegionRepository, GeneratedCourseRepository, ...
```

DB 변경: `V9__create_region_candidates_table.sql` — `region_candidates` 테이블 생성 + 89곳 INSERT 시드.
BaseEntity 규칙에 따라 `created_at`/`updated_at` 포함.

- 컨트롤러는 서비스 호출과 `ResponseEntity.status(CREATED).build()`만 담당한다.
- TourAPI 클라이언트는 현재 recommendation 도메인만 사용하므로 도메인 패키지에 두고, 공유가 필요해지면 `global`로 옮긴다.

## 5. 설정과 의존성

**build.gradle 추가** (Spring Boot 4.0.6 / Java 21 기준):

```groovy
implementation platform("org.springframework.ai:spring-ai-bom:${springAiVersion}")  // Boot 4 호환 버전 확인
implementation 'org.springframework.ai:spring-ai-starter-model-openai'
```

**application.yml 추가**:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7

tour-api:
  base-url: https://apis.data.go.kr/B551011/KorService2
  odii-base-url: https://apis.data.go.kr/B551011/Odii
  service-key: ${TOUR_API_SERVICE_KEY}
```

- TourAPI와 Odii는 같은 공공데이터포털 서비스키를 사용한다. 두 서비스 모두 활용신청 완료됨.
- 테스트용 값은 `application-test.yml`에 더미로 둔다.

## 6. 에러 처리

| 상황 | 처리 |
|---|---|
| 성향 진단 없음 | `PROPENSITY_NOT_FOUND` (404, 기존 코드) |
| TourAPI 호출 실패 (스코어링·후보·상세) | `TOUR_API_UNAVAILABLE` (503, 기존 코드) — 재시도 없이 즉시 실패 |
| LLM 호출 실패 또는 응답 검증 실패 | `RECOMMENDATION_GENERATION_FAILED` (503, **ErrorCode 신규 추가**: "코스 추천 생성에 실패했습니다.") + RecommendationApi `@ApiErrorCodeResponses`에 추가 |
| Odii 호출 실패 | 추천을 실패시키지 않고 오디오 없음으로 진행 (로그만) |

## 7. 테스트

**테스트 코드는 작성하지 않는다** (사용자 결정). 검증은 실제 API 호출을 통한 수동 확인으로 대체한다.

## 8. 비범위 (Non-goals)

- 캐싱, 병렬 호출, 재시도/서킷브레이커, 레이트리밋 대응
- 두루누비 GPX 도보 경로, Green 점수, 인센티브 매칭, TTS 보완 (제안서의 후속 기능)
- Tour API 원본 응답 저장, 지역 특성의 DB 관리
- 비동기 생성(202 + 폴링) 전환 — 현재 계약은 동기 201

## 9. 미해결 리스크

- **Spring AI ↔ Boot 4.0.6 호환 버전**: 구현 시점에 BOM 버전 확인 필요. 호환 이슈가 있으면 OpenAI REST 직접 호출(RestClient)로 대체 가능하도록 CourseComposer 인터페이스를 얇게 유지한다.
- **89곳 시드 데이터의 areaCode/sigunguCode 정확성**: TourAPI `areaCode2` 조회 결과와 대조하는 일회성 확인을 구현 단계에서 수행한 뒤 시드 마이그레이션을 확정한다.
- **응답 시간 40~80초**: 게이트웨이/인프라 타임아웃(예: 60초)이 있으면 초과할 수 있다. MVP 검증 후 필요 시 비동기 전환을 논의한다.
