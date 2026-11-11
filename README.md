# subscription-demo

구독형 서비스의 **정기결제 백엔드** 예제입니다. 상품·요금제 관리, 구독 생명주기, 송장(Invoice) 기반 정기결제, 환불을 다루고, PG는 나이스페이먼츠 빌링키 방식으로 연동합니다.

## 주요 기능

- **상품(Product) · 요금제(Plan)**: 요금제의 이름·등급·가격·결제 주기가 바뀌면 버전이 올라갑니다.
- **구독 생명주기**: 시작, 해지 요청·취소, 활성/비활성 전환
- **선불(PREPAID) / 후불(POSTPAID)** 정산 전략, 결제 기준일을 지정한 **일할 계산**
- **정기결제 스케줄러**: 자정에 송장 생성 → 17시에 결제, 실패한 송장은 14일 이내 재시도
- **빌링키** 등록·삭제, 결제, 전체·부분 환불
- **멀티 테넌트**: 모든 API가 `enterpriseId` 헤더를 받고, 식별자는 `(enterpriseId, id)` 복합키

## 기술 스택

Kotlin 1.9.25 · JDK 21 · Spring Boot 3.5.11 · Spring Data JPA (Hibernate) · QueryDSL 5.1.0 (kapt) · Spring Cloud OpenFeign · MySQL 8 · springdoc-openapi (Swagger UI) · ULID

## 아키텍처

포트 & 어댑터 구조의 멀티 모듈입니다. 도메인과 유스케이스(`core`)는 DB나 PG를 모르고, 인터페이스(포트)만 정의합니다. 구현은 `infrastructure`가 맡습니다.

```mermaid
flowchart LR
    Client([클라이언트]) --> App

    subgraph App[subscription-app]
        C[REST Controller<br/>CORS · Swagger]
    end

    subgraph Core[subscription-core]
        S[Application Service]
        D[Domain Model]
        P{{Port 인터페이스}}
        SCH[Scheduler]
        S --> D
        S --> P
        SCH --> S
    end

    subgraph Infra[subscription-infrastructure]
        A1[Persistence Adapter<br/>JPA · QueryDSL]
        A2[PG Adapter<br/>OpenFeign]
    end

    C --> S
    P -. 구현 .-> A1
    P -. 구현 .-> A2
    A1 --> DB[(MySQL)]
    A2 --> PG[[NicePay]]
```

| 모듈 | 역할 |
|---|---|
| `subscription-core` | 도메인 모델(`Subscription`, `SubscriptionInvoice`, `Payment` …), 유스케이스 서비스, 포트, 스케줄러 |
| `subscription-infrastructure` | 포트 구현: JPA 엔티티·리포지토리(QueryDSL), 나이스페이먼츠 Feign 클라이언트, PG 어댑터 |
| `subscription-app` | REST 컨트롤러, CORS·Swagger 설정, 실행 가능한 `bootJar` |

## 도메인과 정기결제 흐름

### 상태

| 대상 | 상태 |
|---|---|
| 구독 `SubscriptionStatus` | `PENDING` · `ACTIVE` · `PAYMENT_ON_FAILURE` · `INACTIVE` · `TERMINATE_READY` · `TERMINATED` |
| 송장 `InvoiceStatus` | `DRAFT` · `PROCESSING` · `RESOLVED` · `PAYMENT_ON_FAILURE` · `DISCARDED` |

```mermaid
stateDiagram-v2
    [*] --> DRAFT: 송장 생성
    DRAFT --> PROCESSING: 결제 선점
    PAYMENT_ON_FAILURE --> PROCESSING: 재시도 선점
    PROCESSING --> RESOLVED: 결제 성공
    PROCESSING --> PAYMENT_ON_FAILURE: 결제 실패
    PROCESSING --> DISCARDED: 재시도 횟수 초과
    PROCESSING --> PROCESSING: PG 응답 없음 (수동 확인)
    RESOLVED --> [*]
    DISCARDED --> [*]
```

### 스케줄러 (`SubscriptionScheduler`)

| 시각 | 작업 |
|---|---|
| 매일 00:00 | `ACTIVE` 구독 중 오늘이 결제일인 건의 송장(`DRAFT`)을 생성하고, 같은 구독의 송장이 또 생기지 않도록 잠금 |
| 매일 00:00 | 해지 요청(`TERMINATE_READY`)한 선불 구독을 결제일에 해지 완료 처리 |
| 매일 17:00 | 오늘 결제일인 `DRAFT` 송장 + 최근 14일 내 결제 실패 송장을 PG로 결제 |

송장 한 건은 **선점 → PG 호출 → 결과 반영** 순서로 결제합니다. 선점은 송장을 `PROCESSING`으로 바꿔 먼저 커밋하는 단계이고, PG 호출은 트랜잭션 밖에서 합니다. PG 응답을 받지 못하면 결제가 됐는지 알 수 없으므로 송장을 `PROCESSING`으로 남기고 자동으로 다시 결제하지 않습니다(PG 거래 내역과 대조해 정리).

결제 결과에 따라 송장과 구독이 함께 바뀝니다. 성공하면 송장 `RESOLVED`, 구독 `ACTIVE`, 다음 결제일 갱신, 송장 잠금 해제. 실패하면 송장의 재시도 횟수가 오르고 구독은 `PAYMENT_ON_FAILURE`가 되며, 요금제에 정한 재시도 횟수를 넘으면 구독이 `INACTIVE`로 바뀝니다.

## 기술적 설명

### 1. 포트 & 어댑터로 PG 교체 가능성 열어 두기
`BillingKeyPgPort`(core) → `BillingKeyPgAdapter`(infrastructure)가 `VanCompany`에 따라 PG 구현으로 분기합니다. 현재 구현된 PG는 나이스페이먼츠(`NICE_PAYMENTS`)뿐이고, 다른 PG는 어댑터에 분기를 추가하는 방식으로 붙일 수 있게 열어 두었습니다.

### 2. 요금제 스냅샷
구독을 만들 때 요금제를 `PlanSnapshot`(JSON 컬럼)으로 복사해 둡니다. 이후 요금제 가격이 바뀌어도 이미 가입한 구독의 청구 금액은 달라지지 않습니다. 구독 중 계약이 바뀌면 `nextSnapshot`에 담아 두었다가 **다음 결제가 성공한 시점에** 반영합니다.

### 3. 여러 인스턴스에서 동시에 돌아도 송장과 결제는 한 번만
서버를 여러 대 띄우면 같은 시각에 스케줄러가 여러 곳에서 돕니다. 수동 실행 API와 겹칠 수도 있습니다.

**송장 발행** (`SubscriptionInvoiceIssuer`)
- 구독마다 독립 트랜잭션(`REQUIRES_NEW`)으로 처리하고, 트랜잭션 안에서 구독을 다시 읽어 활성 상태·송장 잠금을 확인합니다.
- 구독 엔티티의 `@Version` 낙관적 락: 두 곳이 같은 구독에 송장 잠금을 걸면 늦게 커밋한 쪽이 실패합니다.
- 송장 테이블 유니크 제약 `(enterprise_id, subscription_id, target_billing_at)`: 애플리케이션 검사가 뚫려도 DB가 마지막에 막습니다.

**정기결제** (`InvoiceBillingStep`)
- **선점**: 송장을 `PROCESSING`으로 바꿔 커밋합니다. 송장 엔티티의 `@Version` 때문에 동시에 선점하면 한 곳만 성공하고, 나머지는 건너뜁니다.
- **PG 호출**: 트랜잭션 밖에서 합니다. DB 커넥션을 PG 응답 시간 동안 붙잡지 않습니다.
- **결과 반영**: 송장과 구독을 한 트랜잭션에서 함께 바꿉니다. 한 건이 실패해도 다른 송장의 결과는 그대로 커밋됩니다.

`BillingConcurrencyIntegrationTest`가 실제 MySQL(Testcontainers)에서 8개 스레드로 이를 확인합니다. 같은 시나리오를 이 변경 전 코드에 돌리면 송장 8개 생성, PG 결제 요청 8번이 나갔습니다.

### 4. 금액 계산
금액은 `BigDecimal`로 계산하고, 일할 계산은 소수 10자리까지 나눈 뒤 곱합니다. 마지막에 요금제의 `roundMode`(`HALF_UP` / `FLOOR` / `CEILING`)로 `Long`으로 변환합니다. 결제 회차는 `payVersion`으로 구독·송장·결제에 같이 기록합니다.

### 5. 복합키와 스케줄러 쿼리 인덱스
테넌트를 구분하려고 구독의 키를 `@EmbeddedId (enterprise_id, subscription_id)`로 두었습니다. 스케줄러가 "오늘 결제일인 `ACTIVE` 구독"을 찾으므로 `next_billing_date`를 `next_billing_at`과 별도 컬럼으로 두고 `(enterprise_id, next_billing_date, status)` 인덱스를 걸었습니다.

### 6. PG 연동
빌링키 발급·승인·취소 API를 OpenFeign으로 호출합니다. 빌링키 발급 시 카드 정보는 AES로 암호화하고 SHA-256 서명을 붙입니다. 나이스페이먼츠 응답이 `text/html` + EUC-KR로 오는 JSON이라 Feign 디코더를 따로 구성했습니다.

### 7. 기타
- 식별자는 ULID에 짧은 난수를 붙여 시간순 정렬이 가능합니다.
- JPA 배치 설정(`batch_size`, `rewriteBatchedStatements`, `default_batch_fetch_size`)과 `open-in-view: false`를 사용합니다.
- 테이블은 `ddl-auto: update`로 자동 생성됩니다. 이미 만들어진 DB에서는 `version` 컬럼과 송장 유니크 제약이 추가되며, 기존 데이터에 같은 회차 송장이 중복돼 있으면 유니크 제약이 생성되지 않으니 먼저 정리해야 합니다. API 문서(Swagger UI)는 http://localhost:8080/kl-swagger-ui/index.html 입니다.

> 빌링키 등록·결제·환불처럼 PG를 호출하는 API는 나이스페이먼츠 가맹점 설정이 있어야 동작합니다. 그 외 API(상품·요금제·구독 조회 등)는 PG 없이 확인할 수 있습니다.

## API 개요

모든 API에 `enterpriseId` 헤더가 필요합니다. 전체 목록은 Swagger에서 확인할 수 있습니다.

| 경로 | 설명 |
|---|---|
| `/product` | 구독 상품 등록·수정·활성화·삭제·조회 |
| `/plan` | 요금제 등록·수정·활성화·삭제·조회 |
| `/subscription` | 구독 시작, 해지·해지 취소, 결제일 변경, 활성 전환, 조회. 스케줄러 작업을 수동 실행하는 API 포함 |
| `/invoice` | 송장 조회 |
| `/billing-key` | 빌링키 등록·삭제·조회 |
| `/payment` | 결제, 결제 취소(송장 기준 포함), 조회 |
| `/refund` | 환불 내역 조회 |

## 실행과 테스트

```bash
# 실행: DB 비밀번호는 환경 변수로 넘깁니다 (DB_URL, DB_USERNAME 은 로컬 기본값이 있습니다)
DB_PASSWORD=... ./gradlew :subscription-app:bootRun

# 테스트: 통합 테스트는 Testcontainers 로 MySQL 8 을 띄우므로 Docker 가 필요합니다
./gradlew test
```

| 테스트 | 확인하는 것 |
|---|---|
| `SubscriptionTest` | 활성 전환, 결제 기준일 계산(연말, 31일 기준일, 짧은 달 이후 복귀), 결제 성공·실패 반영 |
| `SubscriptionInvoiceTest` | 재시도 횟수와 폐기, 선점 가능한 상태 |
| `InvoicePricingTest` | 일할 계산 금액과 반올림 방식 |
| `BillingConcurrencyIntegrationTest` | 동시 송장 발행·동시 결제·PG 응답 없음·배치 내 오류 격리, 유니크 제약과 낙관적 락 |

## 남은 과제

- 응답(`ResponseEntity`)과 에러 응답 형식 정리
- `PROCESSING`으로 남은 송장을 PG 거래 조회 API로 대조해 자동 정리하는 배치
- PG 주문번호를 송장 기준으로 고정해 PG 쪽에서도 중복 승인을 거르게 하기
- 결제 실패·결과 미확인 로그를 알림(Slack 등)으로 연결
