# 🎓 온라인 강의 구매 패턴 분석 및 데이터 파이프라인

이 프로젝트는 온라인 강의 플랫폼에서 발생하는 사용자 행동 로그(검색, 결제 단계 등)를 실시간으로 수집하고, 이를 분석 가능한 형태로 가공하여 DB에 적재하는 엔드투엔드 데이터 파이프라인 시스템입니다.

## 🚀 1. 프로젝트 개요
- **목적**: 실시간 이벤트 스트리밍 기반의 ETL(Extract, Transform, Load) 프로세스 구현 및 서비스/분석 데이터 격리.
- **핵심 아키텍처**: 
  - **Producer**: 가상의 사용자 행동 이벤트를 생성하여 Redis Streams에 발행.
  - **Redis Streams**: 고성능 비동기 메시지 브로커 및 이벤트 저장소.
  - **Consumer**: 멀티 데이터소스 설정을 통해 마스터 데이터를 참조하고, 분석 전용 DB에 결과를 적재.
  - **Service DB**: 마스터 데이터(사용자, 강의 등)를 관리하는 운영계 DB.
  - **Analysis DB**: 가공된 이벤트 로그 및 통계 데이터를 관리하는 분석계 DB.

## 🛠 2. 기술 스택
- **Language**: Java 21 (JDK 17 이상 호환)
- **Framework**: Spring Boot 3.2.5
- **Build Tool**: Gradle 8.7 (Multi-module)
- **Message Broker**: Redis (Streams)
- **Database**: MySQL 8.0 (Multi-DataSource 설정)
- **Infra**: Docker, Docker Compose

## 📂 3. 프로젝트 구조
```text
.
├── common      # 공통 도메인 모델, Enum, 이벤트 규격
├── producer    # 이벤트 생성기 (Redis XADD 발행)
├── consumer    # 데이터 파이프라인 (멀티 DB 설정 및 ETL 로직)
│   └── repository
│       ├── service     # 서비스 마스터 DB용 레포지토리
│       └── analysis    # 분석 전용 DB용 레포지토리
└── document    # 프로젝트 요구사항 및 설계 문서
```

## ⚙️ 4. 환경 설정 (Ports)
로컬 환경과의 충돌을 피하기 위해 다음과 같이 포트를 설정하였습니다:
- **Service MySQL**: `3307` (내부 3306)
- **Analysis MySQL**: `3308` (내부 3306)
- **Redis**: `6380` (내부 6379)
- **Producer App**: `8080`
- **Consumer App**: `8081`

---

## 🏃 5. 실행 방법

### 방법 A: 전체 시스템 도커로 실행 (권장)
애플리케이션을 포함한 모든 시스템을 컨테이너 환경에서 실행합니다.
```powershell
-- 먼저 JAR 파일을 빌드합니다.
./gradlew.bat bootJar

-- 전체 서비스를 빌드 및 실행합니다.
docker-compose up --build -d
```

### 방법 B: 로컬에서 애플리케이션 실행
인프라(DB, Redis)만 도커로 띄우고 앱은 로컬에서 직접 실행합니다.
```powershell
-- 인프라 실행
docker-compose up -d service-db analysis-db redis

-- Consumer 실행 (별도 터미널)
./gradlew.bat :consumer:bootRun

-- Producer 실행 (별도 터미널)
./gradlew.bat :producer:bootRun
```

---

## 📊 6. 데이터 확인

### 실시간 로그 확인
애플리케이션 실행 후 콘솔 로그에서 다음과 같은 흐름을 확인할 수 있습니다:
1. `Producer`: `Published event to stream lecture-events: SEARCH`
2. `Consumer`: `Received message from stream: {...}`
3. `Consumer`: `Saved payment analysis to analysis_db for log 1`

### DB 데이터 조회
각 목적에 맞는 DB에 접속하여 데이터를 확인할 수 있습니다.

**1. Analysis DB (Port 3308)**
- **DB**: `analysis_db`
- **주요 테이블**:
  - `raw_event_logs`: 수집된 이벤트 원본 (JSON)
  - `payment_analysis`: 결제 단계별 분석 데이터
  - `search_analysis`: 강의 검색 패턴 분석 데이터

```sql
SELECT * FROM payment_analysis ORDER BY event_time DESC;
```

**2. Service DB (Port 3307)**
- **DB**: `service_db`
- **주요 테이블**: `users`, `courses`, `payments` 등
