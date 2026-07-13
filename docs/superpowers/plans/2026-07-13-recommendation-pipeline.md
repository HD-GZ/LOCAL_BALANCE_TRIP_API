# POST /recommendations 추천 파이프라인 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 사용자 10축 성향 + TourAPI 실시간 집계 + LLM 코스 구성으로 추천 지역(최대 5곳)·코스(지역당 최대 3개)·장소 스냅샷을 생성해 저장하는 `POST /recommendations`를 구현한다.

**Architecture:** 외부 호출(TourAPI 집계→후보 수집→LLM 구성→상세 보강→Odii 매칭)은 트랜잭션 밖에서 순차 실행하고, 완성된 스냅샷만 `RecommendationStore`가 트랜잭션으로 덮어쓴다. LLM은 후보 contentId 안에서 선택만 한다.

**Tech Stack:** Spring Boot 4.0.6 / Java 21 / RestClient / Spring AI(OpenAI, gpt-4o-mini) / Flyway / MySQL

**Spec:** `docs/superpowers/specs/2026-07-13-recommendation-pipeline-design.md`

## Global Constraints

- **테스트 코드를 작성하지 않는다** (사용자 결정). 각 태스크의 검증은 `./gradlew compileJava -q` 성공 + 마지막 태스크의 수동 검증으로 대체한다.
- 캐싱·병렬 호출·재시도·서킷브레이커 금지 (오버엔지니어링 지양). 모든 외부 호출은 순차.
- TourAPI 원본 응답을 DB에 저장하지 않는다. 스냅샷 테이블에 필요한 필드만 저장.
- 지역 5곳·코스 3개는 **최대치(리미트)** — 적어도 실패가 아니다. 단, 지역 0곳/코스 0개면 `RECOMMENDATION_GENERATION_FAILED`.
- KorService2 v4.4 기준: 지역 필터는 법정동 코드 `lDongRegnCd`/`lDongSignguCd` (areaCode 아님).
- 예외는 `BusinessException.of(ErrorCode.X)`, 응답은 `ApiResponseAdvice` 자동 래핑 (컨트롤러에서 수동 래핑 금지).
- 엔티티: `@Getter` + `@NoArgsConstructor(access = PROTECTED)` + private 생성자 + `create(...)` 정적 팩토리, setter 금지.
- 컬럼 제약: region/course `reason` 300자, course/place `name` 100자 — 저장 전 잘라낸다.
- 커밋 메시지는 기존 스타일(한국어, `feat:`/`docs:` prefix)을 따른다.

---

### Task 1: 의존성·설정 (Spring AI, TourAPI 프로퍼티)

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/resources/application.yml`
- Modify: `src/test/resources/application-test.yml`
- Create: `src/main/java/live/lbtrip/global/config/TourApiProperties.java`
- Create: `src/main/java/live/lbtrip/global/config/TourApiConfig.java`

**Interfaces:**
- Produces: `TourApiProperties(String baseUrl, String odiiBaseUrl, String serviceKey)` record — Task 3, 4가 주입받아 사용. `@ConfigurationProperties(prefix = "tour-api")`.

- [ ] **Step 1: build.gradle에 Spring AI 의존성 추가**

`dependencies` 블록 위에 BOM, 블록 안에 스타터 추가:

```groovy
ext {
	set('springAiVersion', "2.0.0")
}

dependencies {
	implementation platform("org.springframework.ai:spring-ai-bom:${springAiVersion}")
	implementation 'org.springframework.ai:spring-ai-starter-model-openai'
	// ... 기존 의존성 유지
}
```

- [ ] **Step 2: 의존성 해석 확인**

Run: `./gradlew dependencies --configuration compileClasspath | grep spring-ai | head -5`
Expected: `spring-ai-openai` 계열 아티팩트가 버전과 함께 출력.
실패(버전 없음) 시: https://spring.io/projects/spring-ai 에서 Boot 4.x 호환 최신 GA 버전을 확인해 `springAiVersion`을 교체한다 (스펙 §9 리스크로 예고된 확인 사항).

- [ ] **Step 3: application.yml에 설정 추가**

`spring:` 트리 안에 `ai`, 최하단에 `tour-api` 추가:

```yaml
spring:
  # (기존 datasource/jpa/flyway 유지)
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

- [ ] **Step 4: application-test.yml에 더미 값 추가** (컨텍스트 로딩 실패 방지)

```yaml
spring:
  ai:
    openai:
      api-key: test-key

tour-api:
  base-url: http://localhost
  odii-base-url: http://localhost
  service-key: test-key
```

- [ ] **Step 5: TourApiProperties + TourApiConfig 작성**

`src/main/java/live/lbtrip/global/config/TourApiProperties.java`:

```java
package live.lbtrip.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tour-api")
public record TourApiProperties(
    String baseUrl,
    String odiiBaseUrl,
    String serviceKey
) {
}
```

`src/main/java/live/lbtrip/global/config/TourApiConfig.java`:

```java
package live.lbtrip.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TourApiProperties.class)
public class TourApiConfig {
}
```

- [ ] **Step 6: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL (출력 없음)

```bash
git add build.gradle src/main/resources/application.yml src/test/resources/application-test.yml \
  src/main/java/live/lbtrip/global/config/TourApiProperties.java \
  src/main/java/live/lbtrip/global/config/TourApiConfig.java
git commit -m "feat: Spring AI 및 TourAPI 설정 추가"
```

---

### Task 2: 인구감소지역 후보 테이블 (엔티티 + Flyway 시드)

**Files:**
- Create: `src/main/java/live/lbtrip/domain/recommendation/model/entity/RegionCandidate.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/repository/RegionCandidateRepository.java`
- Create: `src/main/resources/db/migration/V9__create_region_candidates_table.sql`

**Interfaces:**
- Produces: `RegionCandidate` 엔티티 — getter: `getId()`, `getName()`, `getLdongRegnCd()`, `getLdongSignguCd()`. `RegionCandidateRepository extends JpaRepository<RegionCandidate, Long>` — Task 8이 `findAll()` 사용. Task 3이 `getLdongRegnCd()`/`getLdongSignguCd()`를 쿼리 파라미터로 사용.

- [ ] **Step 1: RegionCandidate 엔티티 작성**

```java
package live.lbtrip.domain.recommendation.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 추천 후보 지역(행안부 지정 인구감소지역). Flyway 시드로 적재되며 코드에서는 조회만 한다.
 */
@Getter
@Entity
@Table(name = "region_candidates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    /** 법정동 시도 코드 (TourAPI lDongRegnCd) */
    @Column(name = "ldong_regn_cd", nullable = false, length = 2)
    private String ldongRegnCd;

    /** 법정동 시군구 코드 (TourAPI lDongSignguCd) */
    @Column(name = "ldong_signgu_cd", nullable = false, length = 3)
    private String ldongSignguCd;
}
```

(생성은 시드 전용이므로 `create(...)` 팩토리를 두지 않는다 — 코드에서 생성할 일이 없다.)

`src/main/java/live/lbtrip/domain/recommendation/repository/RegionCandidateRepository.java`:

```java
package live.lbtrip.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;

public interface RegionCandidateRepository extends JpaRepository<RegionCandidate, Long> {
}
```

- [ ] **Step 2: 법정동 코드 수집 스크립트 실행 (시드 INSERT 생성)**

89곳 지역명은 아래 목록이 확정본이다 (행안부 지정, 군위군은 대구 편입 반영):

- 부산: 동구, 서구, 영도구 / 대구: 남구, 서구, 군위군 / 인천: 강화군, 옹진군 / 경기: 가평군, 연천군
- 강원: 고성군, 삼척시, 양구군, 양양군, 영월군, 정선군, 철원군, 태백시, 평창군, 홍천군, 화천군, 횡성군
- 충북: 괴산군, 단양군, 보은군, 영동군, 옥천군, 제천시
- 충남: 공주시, 금산군, 논산시, 보령시, 부여군, 서천군, 예산군, 청양군, 태안군
- 전북: 고창군, 김제시, 남원시, 무주군, 부안군, 순창군, 임실군, 장수군, 정읍시, 진안군
- 전남: 강진군, 고흥군, 곡성군, 구례군, 담양군, 보성군, 신안군, 영광군, 영암군, 완도군, 장성군, 장흥군, 진도군, 함평군, 해남군, 화순군
- 경북: 고령군, 문경시, 봉화군, 상주시, 성주군, 안동시, 영덕군, 영양군, 영주시, 영천시, 울릉군, 울진군, 의성군, 청도군, 청송군
- 경남: 거창군, 고성군, 남해군, 밀양시, 산청군, 의령군, 창녕군, 하동군, 함안군, 함양군, 합천군

법정동 코드는 추측하지 말고 TourAPI `ldongCode2`에서 받아 생성한다. 스크래치패드에 아래 스크립트를 저장하고 실행 (`TOUR_API_SERVICE_KEY` 환경변수 필요):

```python
# scratchpad/gen_region_seed.py
import json, os, urllib.parse, urllib.request

KEY = os.environ["TOUR_API_SERVICE_KEY"]
BASE = "https://apis.data.go.kr/B551011/KorService2/ldongCode2"

TARGETS = {  # 시도명 부분 문자열 -> [시군구명]
    "부산": ["동구", "서구", "영도구"],
    "대구": ["남구", "서구", "군위군"],
    "인천": ["강화군", "옹진군"],
    "경기": ["가평군", "연천군"],
    "강원": ["고성군", "삼척시", "양구군", "양양군", "영월군", "정선군",
           "철원군", "태백시", "평창군", "홍천군", "화천군", "횡성군"],
    "충청북도": ["괴산군", "단양군", "보은군", "영동군", "옥천군", "제천시"],
    "충청남도": ["공주시", "금산군", "논산시", "보령시", "부여군", "서천군",
             "예산군", "청양군", "태안군"],
    "전북": ["고창군", "김제시", "남원시", "무주군", "부안군", "순창군",
           "임실군", "장수군", "정읍시", "진안군"],
    "전라남도": ["강진군", "고흥군", "곡성군", "구례군", "담양군", "보성군",
             "신안군", "영광군", "영암군", "완도군", "장성군", "장흥군",
             "진도군", "함평군", "해남군", "화순군"],
    "경상북도": ["고령군", "문경시", "봉화군", "상주시", "성주군", "안동시",
             "영덕군", "영양군", "영주시", "영천시", "울릉군", "울진군",
             "의성군", "청도군", "청송군"],
    "경상남도": ["거창군", "고성군", "남해군", "밀양시", "산청군", "의령군",
             "창녕군", "하동군", "함안군", "함양군", "합천군"],
}

def call(params):
    q = {"serviceKey": KEY, "MobileOS": "ETC", "MobileApp": "lbtrip",
         "_type": "json", "numOfRows": "999", "pageNo": "1", **params}
    url = BASE + "?" + urllib.parse.urlencode(q)
    with urllib.request.urlopen(url) as r:
        body = json.load(r)["response"]["body"]["items"]
    return body["item"] if body else []

sidos = call({"lDongListYn": "N"})  # 시도 목록
rows, missing = [], []
for sido in sidos:
    sido_cd, sido_nm = sido["lDongRegnCd"], sido["lDongRegnNm"]
    key = next((k for k in TARGETS if k in sido_nm), None)
    if not key:
        continue
    sggs = call({"lDongListYn": "Y", "lDongRegnCd": sido_cd})
    wanted = set(TARGETS[key])
    for sgg in sggs:
        if sgg["lDongSignguNm"] in wanted:
            rows.append((f"{sido_nm} {sgg['lDongSignguNm']}", sido_cd, sgg["lDongSignguCd"]))
            wanted.discard(sgg["lDongSignguNm"])
    missing.extend(f"{sido_nm} {n}" for n in wanted)

print(f"-- matched {len(rows)} / missing {missing}")
values = ",\n".join(
    f"    ('{n}', '{r}', '{s}', NOW(), NOW())" for n, r, s in sorted(rows, key=lambda x: (x[1], x[2]))
)
print("INSERT INTO region_candidates (name, ldong_regn_cd, ldong_signgu_cd, created_at, updated_at) VALUES\n"
      + values + ";")
```

Run: `TOUR_API_SERVICE_KEY=<디코딩된 키> python3 gen_region_seed.py`
Expected: `-- matched 89 / missing []` 후 INSERT문 출력.
missing이 있으면 해당 시도의 시군구 응답 명칭을 확인해 TARGETS를 보정 후 재실행 (89 매칭될 때까지).

- [ ] **Step 3: V9 마이그레이션 작성**

`src/main/resources/db/migration/V9__create_region_candidates_table.sql` — 테이블 생성 + Step 2 출력 INSERT 붙여넣기:

```sql
CREATE TABLE region_candidates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    ldong_regn_cd VARCHAR(2) NOT NULL,
    ldong_signgu_cd VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_region_candidates_code UNIQUE (ldong_regn_cd, ldong_signgu_cd)
);

-- Step 2 스크립트 출력의 INSERT문 89행을 여기에 붙여넣는다
```

- [ ] **Step 4: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/live/lbtrip/domain/recommendation/model/entity/RegionCandidate.java \
  src/main/java/live/lbtrip/domain/recommendation/repository/RegionCandidateRepository.java \
  src/main/resources/db/migration/V9__create_region_candidates_table.sql
git commit -m "feat: 인구감소지역 후보 테이블 및 시드 추가"
```

---

### Task 3: TourApiClient (지역 집계·장소 목록·상세)

**Files:**
- Create: `src/main/java/live/lbtrip/domain/recommendation/client/TourApiClient.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/client/dto/TourPlace.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/client/dto/RegionStats.java`

**Interfaces:**
- Consumes: `TourApiProperties` (Task 1), `RegionCandidate` (Task 2)
- Produces:
  - `record TourPlace(String contentId, String title, int contentTypeId, String imageUrl, Double longitude, Double latitude)`
  - `record RegionStats(RegionCandidate candidate, int totalCount, Map<Integer, Integer> typeCounts)`
  - `RegionStats fetchRegionStats(RegionCandidate candidate)`
  - `List<TourPlace> fetchPlaces(RegionCandidate candidate, int contentTypeId)`
  - `String fetchOverview(String contentId)` — 없으면 null
  - 모든 호출 실패 시 `BusinessException(TOUR_API_UNAVAILABLE)`

- [ ] **Step 1: DTO record 2개 작성**

`client/dto/TourPlace.java`:

```java
package live.lbtrip.domain.recommendation.client.dto;

/** areaBasedList2 목록 항목의 스냅샷 후보. mapx=경도, mapy=위도(WGS84). */
public record TourPlace(
    String contentId,
    String title,
    int contentTypeId,
    String imageUrl,
    Double longitude,
    Double latitude
) {
}
```

`client/dto/RegionStats.java`:

```java
package live.lbtrip.domain.recommendation.client.dto;

import java.util.Map;

import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;

/** 지역당 1회 표본 조회 결과: 전체 콘텐츠 수 + 표본 100건의 타입 분포. */
public record RegionStats(
    RegionCandidate candidate,
    int totalCount,
    Map<Integer, Integer> typeCounts
) {

    public int sampleSize() {
        return typeCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double typeRatio(int contentTypeId) {
        int sample = sampleSize();
        if (sample == 0) {
            return 0.0;
        }
        return typeCounts.getOrDefault(contentTypeId, 0) / (double) sample;
    }
}
```

- [ ] **Step 2: TourApiClient 작성**

```java
package live.lbtrip.domain.recommendation.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.recommendation.client.dto.RegionStats;
import live.lbtrip.domain.recommendation.client.dto.TourPlace;
import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;
import live.lbtrip.global.config.TourApiProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TourApiClient {

    private static final String RESULT_OK = "0000";

    private final RestClient restClient;
    private final String serviceKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TourApiClient(TourApiProperties properties) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(properties.baseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        this.restClient = RestClient.builder().uriBuilderFactory(uriFactory).build();
        this.serviceKey = properties.serviceKey();
    }

    /** 지역당 1회: totalCount + 표본 100건의 contentTypeId 분포. (스펙 §3-②) */
    public RegionStats fetchRegionStats(RegionCandidate candidate) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", 100)
            .queryParam("arrange", "C")
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        int totalCount = body.path("totalCount").asInt(0);
        Map<Integer, Integer> typeCounts = new HashMap<>();
        for (JsonNode item : items(body)) {
            int typeId = item.path("contenttypeid").asInt(0);
            typeCounts.merge(typeId, 1, Integer::sum);
        }
        return new RegionStats(candidate, totalCount, typeCounts);
    }

    /** 코스 후보 장소 목록. arrange=O는 대표 이미지가 있는 항목만 제목순으로 반환한다. */
    public List<TourPlace> fetchPlaces(RegionCandidate candidate, int contentTypeId) {
        JsonNode body = get("/areaBasedList2", uri -> uri
            .queryParam("numOfRows", 15)
            .queryParam("arrange", "O")
            .queryParam("contentTypeId", contentTypeId)
            .queryParam("lDongRegnCd", candidate.getLdongRegnCd())
            .queryParam("lDongSignguCd", candidate.getLdongSignguCd()));

        List<TourPlace> places = new ArrayList<>();
        for (JsonNode item : items(body)) {
            places.add(new TourPlace(
                item.path("contentid").asText(),
                item.path("title").asText(),
                item.path("contenttypeid").asInt(0),
                item.path("firstimage").asText(null),
                item.path("mapx").isMissingNode() ? null : item.path("mapx").asDouble(),
                item.path("mapy").isMissingNode() ? null : item.path("mapy").asDouble()
            ));
        }
        return places;
    }

    /** detailCommon2에서 overview만 추출. 없으면 null. */
    public String fetchOverview(String contentId) {
        JsonNode body = get("/detailCommon2", uri -> uri.queryParam("contentId", contentId));
        for (JsonNode item : items(body)) {
            String overview = item.path("overview").asText(null);
            if (overview != null && !overview.isBlank()) {
                return overview;
            }
        }
        return null;
    }

    private JsonNode get(String path, java.util.function.UnaryOperator<org.springframework.web.util.UriBuilder> customizer) {
        try {
            String raw = restClient.get()
                .uri(uriBuilder -> customizer.apply(uriBuilder
                        .path(path)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "lbtrip")
                        .queryParam("_type", "json")
                        .queryParam("pageNo", 1))
                    .build())
                .retrieve()
                .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!RESULT_OK.equals(resultCode)) {
                log.error("TourAPI 오류 응답: path={}, resultCode={}", path, resultCode);
                throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
            }
            return root.path("response").path("body");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("TourAPI 호출 실패: path={}", path, e);
            throw BusinessException.of(ErrorCode.TOUR_API_UNAVAILABLE);
        }
    }

    /** items가 빈 문자열("")로 오는 TourAPI 특성 방어: 배열일 때만 순회 대상 반환. */
    private JsonNode items(JsonNode body) {
        JsonNode item = body.path("items").path("item");
        if (item.isArray()) {
            return item;
        }
        return objectMapper.createArrayNode();
    }
}
```

- [ ] **Step 3: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/live/lbtrip/domain/recommendation/client/
git commit -m "feat: TourAPI 클라이언트 추가 (지역 집계·장소 목록·상세 조회)"
```

---

### Task 4: OdiiClient (오디오가이드 매칭용)

**Files:**
- Create: `src/main/java/live/lbtrip/domain/recommendation/client/OdiiClient.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/client/dto/OdiiTheme.java`

**Interfaces:**
- Consumes: `TourApiProperties` (Task 1)
- Produces:
  - `record OdiiTheme(String tid, String tlid, String title, Double longitude, Double latitude)`
  - `List<OdiiTheme> fetchThemesNear(double longitude, double latitude)` — 실패 시 빈 리스트(예외 없음)
  - `String fetchFirstAudioUrl(String tid, String tlid)` — 없거나 실패 시 null

- [ ] **Step 1: OdiiTheme record 작성**

```java
package live.lbtrip.domain.recommendation.client.dto;

/** Odii themeLocationBasedList 항목. 코스 장소와 좌표·제목으로 매칭한다. */
public record OdiiTheme(
    String tid,
    String tlid,
    String title,
    Double longitude,
    Double latitude
) {
}
```

- [ ] **Step 2: OdiiClient 작성**

Odii 실패는 추천 전체를 실패시키지 않는다(스펙 §3-⑥) — 예외 대신 빈 결과 + warn 로그.

```java
package live.lbtrip.domain.recommendation.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.recommendation.client.dto.OdiiTheme;
import live.lbtrip.global.config.TourApiProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OdiiClient {

    private static final int MAX_RADIUS_METERS = 20000; // 명세상 최대 20km

    private final RestClient restClient;
    private final String serviceKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OdiiClient(TourApiProperties properties) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(properties.odiiBaseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        this.restClient = RestClient.builder().uriBuilderFactory(uriFactory).build();
        this.serviceKey = properties.serviceKey();
    }

    public List<OdiiTheme> fetchThemesNear(double longitude, double latitude) {
        try {
            JsonNode body = get("/themeLocationBasedList", uri -> uri
                .queryParam("mapX", longitude)
                .queryParam("mapY", latitude)
                .queryParam("radius", MAX_RADIUS_METERS));

            List<OdiiTheme> themes = new ArrayList<>();
            for (JsonNode item : items(body)) {
                themes.add(new OdiiTheme(
                    item.path("tid").asText(),
                    item.path("tlid").asText(),
                    item.path("title").asText(),
                    item.path("mapX").isMissingNode() ? null : item.path("mapX").asDouble(),
                    item.path("mapY").isMissingNode() ? null : item.path("mapY").asDouble()
                ));
            }
            return themes;
        } catch (Exception e) {
            log.warn("Odii 테마 조회 실패 — 오디오 없이 진행", e);
            return List.of();
        }
    }

    public String fetchFirstAudioUrl(String tid, String tlid) {
        try {
            JsonNode body = get("/storyBasedList", uri -> uri
                .queryParam("tid", tid)
                .queryParam("tlid", tlid));

            for (JsonNode item : items(body)) {
                String audioUrl = item.path("audioUrl").asText(null);
                if (audioUrl != null && !audioUrl.isBlank()) {
                    return audioUrl;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Odii 스토리 조회 실패 — 오디오 없이 진행: tid={}", tid, e);
            return null;
        }
    }

    private JsonNode get(String path, java.util.function.UnaryOperator<org.springframework.web.util.UriBuilder> customizer) throws Exception {
        String raw = restClient.get()
            .uri(uriBuilder -> customizer.apply(uriBuilder
                    .path(path)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "lbtrip")
                    .queryParam("_type", "json")
                    .queryParam("langCode", "ko")
                    .queryParam("numOfRows", 100)
                    .queryParam("pageNo", 1))
                .build())
            .retrieve()
            .body(String.class);

        JsonNode root = objectMapper.readTree(raw);
        return root.path("response").path("body");
    }

    private JsonNode items(JsonNode body) {
        JsonNode item = body.path("items").path("item");
        if (item.isArray()) {
            return item;
        }
        return objectMapper.createArrayNode();
    }
}
```

- [ ] **Step 3: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/live/lbtrip/domain/recommendation/client/OdiiClient.java \
  src/main/java/live/lbtrip/domain/recommendation/client/dto/OdiiTheme.java
git commit -m "feat: Odii 오디오가이드 클라이언트 추가"
```

---

### Task 5: RegionScorer (성향 매칭 점수)

**Files:**
- Create: `src/main/java/live/lbtrip/domain/recommendation/service/RegionScorer.java`

**Interfaces:**
- Consumes: `Propensity`(getPreference/getValueConsumption, 각 축 int 1~5), `RegionStats` (Task 3)
- Produces: `List<RegionStats> selectTop(Propensity propensity, List<RegionStats> statsList, int limit)` — 점수 내림차순 상위 limit개. Task 8이 사용.

> **튜닝 포인트**: 아래 시그널→축 매핑과 가중치 공식(스펙 §3-②)은 추천 품질의 핵심이며, 수동 검증 후 조정 가능성이 가장 높은 지점이다.

- [ ] **Step 1: RegionScorer 작성**

```java
package live.lbtrip.domain.recommendation.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.recommendation.client.dto.RegionStats;

/**
 * TourAPI 시그널(콘텐츠 규모·타입 분포)과 사용자 성향의 매칭 점수로 지역을 정렬한다. (스펙 §3-②)
 * sociality·transportation은 대응 시그널이 없어 제외한다.
 */
@Component
public class RegionScorer {

    private static final int TOURIST_SPOT = 12;
    private static final int CULTURE = 14;
    private static final int LEPORTS = 28;
    private static final int STAY = 32;
    private static final int SHOPPING = 38;
    private static final int FOOD = 39;

    public List<RegionStats> selectTop(Propensity propensity, List<RegionStats> statsList, int limit) {
        double[] rarity = normalize(statsList.stream()
            .mapToDouble(stats -> -stats.totalCount()).toArray()); // 콘텐츠가 적을수록 1
        double[] culture = normalize(ratios(statsList, CULTURE));
        double[] leports = normalize(ratios(statsList, LEPORTS));
        double[] stay = normalize(ratios(statsList, STAY));
        double[] shopping = normalize(ratios(statsList, SHOPPING));
        double[] food = normalize(ratios(statsList, FOOD));

        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

        record Scored(RegionStats stats, double score) {
        }

        return java.util.stream.IntStream.range(0, statsList.size())
            .mapToObj(i -> new Scored(statsList.get(i),
                weight(preference.getLocality()) * rarity[i]
                    + weight(preference.getFrugality()) * shopping[i]
                    + weight(preference.getExperientiality()) * culture[i]
                    + weight(preference.getVitality()) * leports[i]
                    + weight(consumption.getFood()) * food[i]
                    + weight(consumption.getCafeExhibition()) * culture[i]
                    + weight(consumption.getExperience()) * leports[i]
                    + weight(consumption.getAccommodation()) * stay[i]))
            .sorted(Comparator.comparingDouble(Scored::score).reversed())
            .limit(limit)
            .map(Scored::stats)
            .toList();
    }

    /** 원점수 1~5 → 가중치 -2~+2. 3(중립)이면 해당 시그널 무시. */
    private double weight(int score) {
        return score - 3;
    }

    private double[] ratios(List<RegionStats> statsList, int contentTypeId) {
        return statsList.stream().mapToDouble(stats -> stats.typeRatio(contentTypeId)).toArray();
    }

    /** min-max 정규화. 전 지역 값이 같으면 전부 0. */
    private double[] normalize(double[] values) {
        double min = java.util.Arrays.stream(values).min().orElse(0);
        double max = java.util.Arrays.stream(values).max().orElse(0);
        double range = max - min;
        double[] normalized = new double[values.length];
        if (range == 0) {
            return normalized;
        }
        for (int i = 0; i < values.length; i++) {
            normalized[i] = (values[i] - min) / range;
        }
        return normalized;
    }
}
```

- [ ] **Step 2: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/live/lbtrip/domain/recommendation/service/RegionScorer.java
git commit -m "feat: 성향 기반 지역 스코어링 추가"
```

---

### Task 6: ErrorCode 추가 + CourseComposer (LLM 코스 구성)

**Files:**
- Modify: `src/main/java/live/lbtrip/global/error/ErrorCode.java` (INTERNAL_SERVER_ERROR 위에 추가)
- Modify: `src/main/java/live/lbtrip/domain/recommendation/controller/RecommendationApi.java` (createRecommendations의 @ApiErrorCodeResponses)
- Create: `src/main/java/live/lbtrip/domain/recommendation/service/CourseComposer.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/service/dto/CourseComposition.java`

**Interfaces:**
- Consumes: `TourPlace` (Task 3), Spring AI `ChatClient.Builder` (Task 1 스타터가 자동 구성)
- Produces:
  - `record CourseComposition(String regionReason, List<CoursePlan> courses)` / `record CoursePlan(String name, String reason, List<String> placeContentIds)` (CoursePlan은 CourseComposition의 중첩 record)
  - `CourseComposition compose(Propensity propensity, String regionName, List<TourPlace> candidates)` — 검증 완료본 반환. Task 8이 사용.
  - `ErrorCode.RECOMMENDATION_GENERATION_FAILED`

- [ ] **Step 1: ErrorCode 추가**

`ErrorCode.java`의 `INCENTIVE_NOT_FOUND` 다음 줄에:

```java
    RECOMMENDATION_GENERATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "코스 추천 생성에 실패했습니다."),
```

`RecommendationApi.java`의 `createRecommendations` 위 `@ApiErrorCodeResponses`에 추가 (static import `RECOMMENDATION_GENERATION_FAILED`도):

```java
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        PROPENSITY_NOT_FOUND,
        TOUR_API_UNAVAILABLE,
        RECOMMENDATION_GENERATION_FAILED
    })
```

- [ ] **Step 2: CourseComposition record 작성**

`service/dto/CourseComposition.java`:

```java
package live.lbtrip.domain.recommendation.service.dto;

import java.util.List;

/** LLM 구조화 출력 계약. placeContentIds는 후보 목록의 contentId만 허용된다. (스펙 §3-④) */
public record CourseComposition(
    String regionReason,
    List<CoursePlan> courses
) {

    public record CoursePlan(
        String name,
        String reason,
        List<String> placeContentIds
    ) {
    }
}
```

- [ ] **Step 3: CourseComposer 작성**

```java
package live.lbtrip.domain.recommendation.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.recommendation.client.dto.TourPlace;
import live.lbtrip.domain.recommendation.service.dto.CourseComposition;
import live.lbtrip.domain.recommendation.service.dto.CourseComposition.CoursePlan;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM은 후보 장소 집합 안에서 "선택"과 "문구 창작"만 한다.
 * 후보에 없는 contentId는 걸러내므로 환각이 저장 경로에 들어올 수 없다. (스펙 §3-④)
 */
@Slf4j
@Component
public class CourseComposer {

    private static final int MAX_COURSES = 3;
    private static final int MIN_PLACES_PER_COURSE = 2;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int REASON_MAX_LENGTH = 300;

    private static final java.util.Map<Integer, String> TYPE_NAMES = java.util.Map.of(
        12, "관광지", 14, "문화시설", 28, "레포츠", 38, "쇼핑", 39, "음식점"
    );

    private final ChatClient chatClient;

    public CourseComposer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public CourseComposition compose(Propensity propensity, String regionName, List<TourPlace> candidates) {
        CourseComposition raw;
        try {
            raw = chatClient.prompt()
                .user(buildPrompt(propensity, regionName, candidates))
                .call()
                .entity(CourseComposition.class);
        } catch (Exception e) {
            log.error("LLM 코스 구성 호출 실패: region={}", regionName, e);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        return validate(raw, candidates, regionName);
    }

    private String buildPrompt(Propensity propensity, String regionName, List<TourPlace> candidates) {
        Preference preference = propensity.getPreference();
        ValueConsumption consumption = propensity.getValueConsumption();

        String candidateLines = candidates.stream()
            .map(place -> "%s | %s | %s".formatted(
                place.contentId(),
                TYPE_NAMES.getOrDefault(place.contentTypeId(), "기타"),
                place.title()))
            .collect(Collectors.joining("\n"));

        return """
            당신은 한국 로컬 여행 코스 큐레이터입니다. 아래 사용자 성향과 후보 장소만 사용해
            "%s" 지역의 여행 코스를 설계하세요.

            [사용자 성향 — 각 1~5점, 5에 가까울수록 오른쪽 성향]
            - 로컬 선호(핫플↔로컬): %d
            - 실속 소비(럭셔리↔실속): %d
            - 생활 체험 선호(관람형↔생활체험): %d
            - 활동성(휴식형↔활동형): %d
            - 동행 성향(혼행↔세대동행): %d
            [가치소비 — 각 1~5점, 5에 가까울수록 그 항목에 투자]
            - 숙소: %d / 음식: %d / 체험: %d / 이동: %d / 카페·전시: %d

            [후보 장소 — "contentId | 타입 | 이름", 이 목록의 contentId만 사용할 것]
            %s

            [요구사항]
            1. 코스는 최대 %d개, 가능하면 %d개. 각 코스는 서로 다른 테마(예: 미식/힐링·산책/생활 체험)로,
               사용자 점수가 높은 축을 테마에 반영할 것.
            2. 각 코스의 placeContentIds는 위 후보 목록의 contentId만, 방문 순서대로 3~5개.
               같은 코스의 장소는 도보 이동을 고려해 서로 가깝게 묶을 것.
            3. regionReason(300자 이내): 이 지역이 사용자 성향과 왜 맞는지 한두 문장.
               문체 예시: "관광객 발길이 드문 한적한 로컬 분위기 · 전통시장 등 실속 여행 인프라 — 지금 취향과 잘 맞는 지역이에요"
            4. 각 코스 reason(300자 이내) 문체 예시: "실속 소비 + 로컬 미식 성향을 반영해 노포·골목 상권 위주로 짰어요"
            5. 코스 name은 "%s"로 시작하고 테마를 담을 것 (100자 이내). 예: "%s 골목 미식 코스"
            """.formatted(
            regionName,
            preference.getLocality(), preference.getFrugality(), preference.getExperientiality(),
            preference.getVitality(), preference.getSociality(),
            consumption.getAccommodation(), consumption.getFood(), consumption.getExperience(),
            consumption.getTransportation(), consumption.getCafeExhibition(),
            candidateLines, MAX_COURSES, MAX_COURSES, regionName, regionName);
    }

    private CourseComposition validate(CourseComposition raw, List<TourPlace> candidates, String regionName) {
        if (raw == null || raw.courses() == null || raw.courses().isEmpty()) {
            log.error("LLM 응답에 코스 없음: region={}", regionName);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        Set<String> validIds = candidates.stream().map(TourPlace::contentId).collect(Collectors.toSet());

        List<CoursePlan> courses = raw.courses().stream()
            .map(course -> new CoursePlan(
                truncate(course.name(), NAME_MAX_LENGTH),
                truncate(course.reason(), REASON_MAX_LENGTH),
                course.placeContentIds() == null ? List.<String>of()
                    : course.placeContentIds().stream().filter(validIds::contains).distinct().toList()))
            .filter(course -> course.placeContentIds().size() >= MIN_PLACES_PER_COURSE)
            .limit(MAX_COURSES)
            .toList();

        if (courses.isEmpty()) {
            log.error("LLM 코스가 검증에서 전부 탈락: region={}", regionName);
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        return new CourseComposition(truncate(raw.regionReason(), REASON_MAX_LENGTH), courses);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
```

- [ ] **Step 4: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/live/lbtrip/global/error/ErrorCode.java \
  src/main/java/live/lbtrip/domain/recommendation/controller/RecommendationApi.java \
  src/main/java/live/lbtrip/domain/recommendation/service/CourseComposer.java \
  src/main/java/live/lbtrip/domain/recommendation/service/dto/CourseComposition.java
git commit -m "feat: LLM 코스 구성 컴포넌트 및 추천 생성 실패 에러코드 추가"
```

---

### Task 7: WalkTimeCalculator (하버사인 도보 시간)

**Files:**
- Create: `src/main/java/live/lbtrip/domain/recommendation/service/WalkTimeCalculator.java`

**Interfaces:**
- Produces: `static Integer walkMinutes(Double fromLon, Double fromLat, Double toLon, Double toLat)` — 좌표 하나라도 null이면 null, 아니면 하버사인 거리 ÷ 67m/분 반올림(최소 1분). Task 8이 사용.

- [ ] **Step 1: WalkTimeCalculator 작성**

```java
package live.lbtrip.domain.recommendation.service;

/** 인접 장소 간 도보 시간 추정: 하버사인 거리 ÷ 67m/분(4km/h). (스펙 §3-⑦) */
public final class WalkTimeCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final double WALK_METERS_PER_MINUTE = 67;

    private WalkTimeCalculator() {
    }

    public static Integer walkMinutes(Double fromLon, Double fromLat, Double toLon, Double toLat) {
        if (fromLon == null || fromLat == null || toLon == null || toLat == null) {
            return null;
        }
        double distance = haversineMeters(fromLat, fromLon, toLat, toLon);
        return Math.max(1, (int) Math.round(distance / WALK_METERS_PER_MINUTE));
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
```

- [ ] **Step 2: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/live/lbtrip/domain/recommendation/service/WalkTimeCalculator.java
git commit -m "feat: 하버사인 기반 도보 시간 계산 추가"
```

---

### Task 8: RecommendationService 오케스트레이터 + 저장 + 컨트롤러 연결

**Files:**
- Create: `src/main/java/live/lbtrip/domain/recommendation/repository/RecommendedRegionRepository.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/service/RecommendationStore.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/service/RecommendationService.java`
- Create: `src/main/java/live/lbtrip/domain/recommendation/service/dto/RegionPlan.java`
- Modify: `src/main/java/live/lbtrip/domain/recommendation/controller/RecommendationController.java` (createRecommendations만)

**Interfaces:**
- Consumes: Task 2~7의 모든 Produces (`RegionCandidateRepository.findAll()`, `TourApiClient.fetchRegionStats/fetchPlaces/fetchOverview`, `OdiiClient.fetchThemesNear/fetchFirstAudioUrl`, `RegionScorer.selectTop`, `CourseComposer.compose`, `WalkTimeCalculator.walkMinutes`), `PropensityRepository.findByUserId`, `UserRepository.getReferenceById`
- Produces: `RecommendationService.createRecommendations(Long userId)` — 컨트롤러가 호출. `RecommendationStore.replace(Long userId, List<RegionPlan> plans)` — @Transactional 저장.

- [ ] **Step 1: RecommendedRegionRepository 작성**

```java
package live.lbtrip.domain.recommendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;

public interface RecommendedRegionRepository extends JpaRepository<RecommendedRegion, Long> {

    List<RecommendedRegion> findAllByUserIdOrderByDisplayOrder(Long userId);
}
```

- [ ] **Step 2: 중간 산출물 RegionPlan 작성**

외부 호출 결과를 트랜잭션 밖에서 모두 조립해 담는 그릇. `service/dto/RegionPlan.java`:

```java
package live.lbtrip.domain.recommendation.service.dto;

import java.util.List;

/** 저장 직전 완성 스냅샷: 외부 API·LLM 결과가 모두 반영된 지역 1곳 분량. */
public record RegionPlan(
    String regionName,
    String imageUrl,
    String reason,
    List<CoursePlanData> courses
) {

    public record CoursePlanData(
        String name,
        String reason,
        String imageUrl,
        List<PlaceSnapshot> places
    ) {
    }

    public record PlaceSnapshot(
        int visitOrder,
        String name,
        String overview,
        String imageUrl,
        Double latitude,
        Double longitude,
        Integer walkMinutes,
        boolean hasAudio,
        String audioUrl
    ) {
    }
}
```

- [ ] **Step 3: RecommendationStore 작성 (트랜잭션 저장)**

`@Transactional`은 이 빈에만 둔다 — 외부 호출이 트랜잭션을 점유하지 않게 하는 스펙 §3-⑧의 핵심.

```java
package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.recommendation.service.dto.RegionPlan;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/** 완성된 RegionPlan들을 기존 추천과 원자적으로 교체한다. (스펙 §3-⑧) */
@Component
@RequiredArgsConstructor
public class RecommendationStore {

    private final RecommendedRegionRepository recommendedRegionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void replace(Long userId, List<RegionPlan> plans) {
        List<RecommendedRegion> existing = recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId);
        recommendedRegionRepository.deleteAll(existing); // cascade로 courses/places까지 삭제
        recommendedRegionRepository.flush();

        User userRef = userRepository.getReferenceById(userId);
        int displayOrder = 1;
        for (RegionPlan plan : plans) {
            RecommendedRegion region = RecommendedRegion.create(
                userRef, plan.regionName(), plan.imageUrl(), plan.reason(), displayOrder++);

            for (RegionPlan.CoursePlanData courseData : plan.courses()) {
                GeneratedCourse course = GeneratedCourse.create(
                    userRef, courseData.name(), courseData.reason(), courseData.imageUrl());
                region.addCourse(course);

                for (RegionPlan.PlaceSnapshot place : courseData.places()) {
                    course.addPlace(CoursePlace.create(
                        place.visitOrder(), place.name(), place.overview(), place.imageUrl(),
                        place.latitude(), place.longitude(), place.walkMinutes(),
                        place.hasAudio(), place.audioUrl()));
                }
            }
            recommendedRegionRepository.save(region);
        }
    }
}
```

- [ ] **Step 4: RecommendationService 작성 (오케스트레이터)**

```java
package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.recommendation.client.OdiiClient;
import live.lbtrip.domain.recommendation.client.TourApiClient;
import live.lbtrip.domain.recommendation.client.dto.OdiiTheme;
import live.lbtrip.domain.recommendation.client.dto.RegionStats;
import live.lbtrip.domain.recommendation.client.dto.TourPlace;
import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;
import live.lbtrip.domain.recommendation.repository.RegionCandidateRepository;
import live.lbtrip.domain.recommendation.service.dto.CourseComposition;
import live.lbtrip.domain.recommendation.service.dto.RegionPlan;
import live.lbtrip.domain.recommendation.service.dto.RegionPlan.CoursePlanData;
import live.lbtrip.domain.recommendation.service.dto.RegionPlan.PlaceSnapshot;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * POST /recommendations 파이프라인 오케스트레이터. (스펙 §2)
 * 외부 호출은 전부 트랜잭션 밖(이 클래스), 저장만 RecommendationStore 트랜잭션.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int MAX_REGIONS = 5;
    private static final List<Integer> CANDIDATE_CONTENT_TYPES = List.of(12, 14, 28, 38, 39);
    private static final double AUDIO_MATCH_RADIUS_METERS = 500;

    private final PropensityRepository propensityRepository;
    private final RegionCandidateRepository regionCandidateRepository;
    private final TourApiClient tourApiClient;
    private final OdiiClient odiiClient;
    private final RegionScorer regionScorer;
    private final CourseComposer courseComposer;
    private final RecommendationStore recommendationStore;

    public void createRecommendations(Long userId) {
        // ① 성향 로드
        Propensity propensity = propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));

        // ② 지역 스코어링
        List<RegionStats> statsList = new ArrayList<>();
        for (RegionCandidate candidate : regionCandidateRepository.findAll()) {
            statsList.add(tourApiClient.fetchRegionStats(candidate));
        }
        List<RegionStats> selected = regionScorer.selectTop(propensity, statsList, MAX_REGIONS);

        // ③~⑦ 지역별 코스 조립
        List<RegionPlan> plans = new ArrayList<>();
        for (RegionStats regionStats : selected) {
            RegionPlan plan = buildRegionPlan(propensity, regionStats.candidate());
            if (plan != null) {
                plans.add(plan);
            }
        }
        if (plans.isEmpty()) {
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        // ⑧ 저장 (트랜잭션)
        recommendationStore.replace(userId, plans);
    }

    private RegionPlan buildRegionPlan(Propensity propensity, RegionCandidate region) {
        // ③ 장소 후보 수집
        Map<String, TourPlace> candidatesById = new LinkedHashMap<>();
        for (int contentTypeId : CANDIDATE_CONTENT_TYPES) {
            for (TourPlace place : tourApiClient.fetchPlaces(region, contentTypeId)) {
                candidatesById.putIfAbsent(place.contentId(), place);
            }
        }
        if (candidatesById.isEmpty()) {
            log.warn("후보 장소 없음 — 지역 제외: {}", region.getName());
            return null;
        }

        // ④ LLM 코스 구성
        List<TourPlace> candidates = List.copyOf(candidatesById.values());
        CourseComposition composition = courseComposer.compose(propensity, region.getName(), candidates);

        // ⑥ 오디오 매칭 준비: 지역당 1회, 후보 좌표 평균 중심
        List<OdiiTheme> themes = odiiClient.fetchThemesNear(
            averageLongitude(candidates), averageLatitude(candidates));

        List<CoursePlanData> courses = new ArrayList<>();
        for (CourseComposition.CoursePlan coursePlan : composition.courses()) {
            courses.add(buildCourse(coursePlan, candidatesById, themes));
        }

        String regionImage = courses.getFirst().imageUrl();
        return new RegionPlan(region.getName(), regionImage, composition.regionReason(), courses);
    }

    private CoursePlanData buildCourse(
        CourseComposition.CoursePlan coursePlan,
        Map<String, TourPlace> candidatesById,
        List<OdiiTheme> themes
    ) {
        List<PlaceSnapshot> places = new ArrayList<>();
        TourPlace previous = null;
        int visitOrder = 1;
        for (String contentId : coursePlan.placeContentIds()) {
            TourPlace place = candidatesById.get(contentId);
            // ⑤ 상세 보강 (선택된 장소만)
            String overview = tourApiClient.fetchOverview(contentId);
            // ⑦ 도보 시간
            Integer walkMinutes = previous == null ? null : WalkTimeCalculator.walkMinutes(
                previous.longitude(), previous.latitude(), place.longitude(), place.latitude());
            // ⑥ 오디오 매칭
            String audioUrl = matchAudio(place, themes);

            places.add(new PlaceSnapshot(
                visitOrder++, place.title(), overview, place.imageUrl(),
                place.latitude(), place.longitude(), walkMinutes,
                audioUrl != null, audioUrl));
            previous = place;
        }
        String courseImage = places.getFirst().imageUrl();
        return new CoursePlanData(coursePlan.name(), coursePlan.reason(), courseImage, places);
    }

    /** 좌표 500m 이내 + 공백 제거 제목 포함 관계면 매칭. (스펙 §3-⑥) */
    private String matchAudio(TourPlace place, List<OdiiTheme> themes) {
        if (place.longitude() == null || place.latitude() == null) {
            return null;
        }
        String placeTitle = normalizeTitle(place.title());
        for (OdiiTheme theme : themes) {
            if (theme.longitude() == null || theme.latitude() == null) {
                continue;
            }
            Integer minutes = WalkTimeCalculator.walkMinutes(
                place.longitude(), place.latitude(), theme.longitude(), theme.latitude());
            boolean near = minutes != null && minutes * 67.0 <= AUDIO_MATCH_RADIUS_METERS;
            String themeTitle = normalizeTitle(theme.title());
            boolean titleMatched = placeTitle.contains(themeTitle) || themeTitle.contains(placeTitle);
            if (near && titleMatched) {
                return odiiClient.fetchFirstAudioUrl(theme.tid(), theme.tlid());
            }
        }
        return null;
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.replaceAll("\\s", "");
    }

    private double averageLongitude(List<TourPlace> places) {
        return places.stream().filter(p -> p.longitude() != null)
            .mapToDouble(TourPlace::longitude).average().orElse(0);
    }

    private double averageLatitude(List<TourPlace> places) {
        return places.stream().filter(p -> p.latitude() != null)
            .mapToDouble(TourPlace::latitude).average().orElse(0);
    }
}
```

- [ ] **Step 5: 컨트롤러 연결**

`RecommendationController.java`에서 `createRecommendations`만 수정 (나머지 GET 더미는 이 브랜치 범위 아님):

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController implements RecommendationApi {

    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<Void> createRecommendations(@UserId Long userId) {
        recommendationService.createRecommendations(userId);
        return ResponseEntity.status(CREATED).build();
    }
    // ... 이하 기존 GET/save 메서드 유지
```

import 추가: `lombok.RequiredArgsConstructor`, `live.lbtrip.domain.recommendation.service.RecommendationService`.

- [ ] **Step 6: 컴파일 확인 후 커밋**

Run: `./gradlew compileJava -q` → Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/live/lbtrip/domain/recommendation/
git commit -m "feat: 코스 추천 생성 파이프라인 구현 (POST /recommendations)"
```

---

### Task 9: 수동 검증 (엔드투엔드)

**Files:** 없음 (검증만)

- [ ] **Step 1: 로컬 기동**

로컬 MySQL과 실제 키가 필요하다. 기존 로컬 실행 env에 두 개를 추가해 기동:

```bash
OPENAI_API_KEY=<실키> TOUR_API_SERVICE_KEY=<디코딩된 실키> ./gradlew bootRun
# (기존 DB_URL, DB_USERNAME, DB_PASSWORD, JWT_* 등 로컬 env는 기존 방식대로)
```

Expected: 기동 로그에 Flyway `V9` 적용, 오류 없음. `region_candidates`에 89행 확인:
`SELECT COUNT(*) FROM region_candidates;` → 89

- [ ] **Step 2: 사용자 준비 → 성향 등록 → 추천 생성 호출**

기존 인증 플로우로 액세스 토큰 확보 후:

```bash
# 성향 등록 (PropensityRequest 형식은 Swagger /swagger-ui 확인)
curl -s -X POST http://localhost:8080/propensities \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"locality":5,"frugality":4,"experientiality":3,"vitality":2,"sociality":1,
       "accommodation":2,"food":5,"experience":4,"transportation":3,"cafeExhibition":3}'

# 추천 생성 (수십 초 소요)
time curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/recommendations \
  -H "Authorization: Bearer $TOKEN"
```

Expected: `201`, 소요 시간 40~120초.

- [ ] **Step 3: 저장 결과 확인 (조회 API는 아직 더미이므로 DB 직접 확인)**

```sql
SELECT region_name, reason, display_order FROM recommended_regions ORDER BY display_order;
SELECT r.region_name, c.name, c.reason FROM generated_courses c
  JOIN recommended_regions r ON r.id = c.recommended_region_id;
SELECT course_id, visit_order, name, walk_minutes, has_audio,
       LEFT(overview, 40) AS overview_head
  FROM course_places ORDER BY course_id, visit_order;
```

Expected 체크리스트:
- 지역 1~5곳, `reason`이 자연스러운 한국어이고 성향(예: 미식 5점 → 미식 언급)과 부합
- 지역당 코스 1~3개, 코스명이 "지역명 ... 코스" 형태
- 장소 2~5곳/코스, `visit_order` 1부터 연속, 첫 장소 `walk_minutes` NULL
- `overview`가 실제 TourAPI 소개 문장 (LLM 창작 아님)
- 같은 사용자로 한 번 더 POST → 지역/코스/장소가 전부 교체되고 행이 누적되지 않음

- [ ] **Step 4: 이상 발견 시 튜닝 포인트**

- 지역 추천이 성향과 안 맞음 → Task 5 `RegionScorer`의 축-시그널 매핑/가중치 조정
- 코스 문구 품질 낮음 → Task 6 프롬프트의 문체 예시·요구사항 보강
- 특정 지역 후보 부족으로 자주 탈락 → Task 3 `fetchPlaces`의 `numOfRows` 상향(15→20)

---

## Self-Review 결과

- **스펙 커버리지**: §3-①~⑧ → Task 8(①,②호출,③~⑧ 오케스트레이션), Task 2(후보 풀), Task 3~4(클라이언트), Task 5(스코어링), Task 6(LLM+검증+ErrorCode), Task 7(도보 시간), §5(Task 1), §6(Task 3·6·8), §7 수동 검증(Task 9). 비범위 항목 미포함 확인.
- **타입 일관성**: `RegionStats`/`TourPlace`/`OdiiTheme`/`CourseComposition`/`RegionPlan` 시그니처가 생산 태스크와 소비 태스크(8) 간 일치함을 교차 확인.
- **플레이스홀더**: 시드 INSERT만 스크립트 출력 붙여넣기로 남김 — 코드를 추측으로 하드코딩하지 않기 위한 의도적 설계(스펙 §9 리스크 대응)이며, 생성 스크립트 전문을 포함했으므로 실행 가능.
