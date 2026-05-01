# 🎓 실시간 강의 구매 패턴 분석 데이터 파이프라인
> **Analyzing Course Purchase Patterns & Identifying Pain Points**

이 프로젝트는 온라인 강의 플랫폼에서 발생하는 다양한 사용자 행동을 실시간으로 수집하고 가공하여 적재하는 엔드투엔드 데이터 파이프라인입니다.

---

## 🏃 1. 실행 방법

### 필요한 도구
- **Docker Desktop**: 컨테이너 기반 인프라 및 앱 실행

### 실행 명령어 (단 한 줄로 시작)
```powershell
docker-compose up --build -d
```
> **참고**: Multi-stage Build를 통해 Docker 내부에서 자동으로 소스 코드를 빌드하므로, 별도의 JDK 설치나 Gradle 빌드가 필요하지 않습니다.

---

## 📊 2. 스키마 설명

### 설계 이유
운영계(Service DB)의 무결성을 유지하면서도, 분석계(Analysis DB)에 **나이, 성별, 플랫폼, 지역 등**의 다차원 데이터를 이벤트 시점에 스냅샷으로 함께 적재하도록 설계했습니다. 이를 통해 운영 DB에 대한 부하 없이 복잡한 집계 쿼리를 독립적으로 수행하고 시간 흐름에 따른 사용자 행동 변화를 정확히 추적할 수 있습니다.

### 테이블 구조 요약

#### **[Service DB] - 운영 마스터 데이터**
| 테이블명 | 용도 | 주요 컬럼 |
| :--- | :--- | :--- |
| `users` | 사용자 정보 | `id`, `email`, `role`, `gender`, `age` |
| `courses` | 강의 정보 | `id`, `teacher`, `level`, `category`, `price` |
| `payments` | 결제 마스터 | `id`, `userId`, `courseId`, `status` |

#### **[Analysis DB] - 분석 및 로그 데이터**
| 테이블명 | 용도 | 주요 컬럼 |
| :--- | :--- | :--- |
| `raw_event_logs` | 원본 이벤트 수집 | `id`, `data (JSON)`, `createdAt` |
| `payment_analysis` | 결제 퍼널 분석 | `userId`, `courseId`, `amount`, `status`, `age`, `gender`, `platform`, `region` |
| `search_analysis` | 검색 패턴 분석 | `userId`, `keyword`, `resultCount`, `viewedCourseId`, `age`, `gender` |

---

## 🤔 3. 구현하면서 고민한 점

### 서비스 DB와 분석 DB의 물리적 분리
- **문제**: 한 DB에 모든 데이터를 넣을 경우 분석가의 무거운 쿼리가 실제 사용자 결제 서비스에 영향을 줄 수 있었습니다.
- **결정**: Spring Boot의 **Multi-DataSource** 기능을 사용하여 운영계(`service_db`)와 분석계(`analysis_db`)를 물리적으로 분리된 MariaDB 인스턴스로 구축했습니다.

### Docker 환경에서의 I/O 병목 해결
- **문제**: 로컬 환경의 Docker 디스크 I/O 속도 문제로 인해 MySQL 초기화 과정에서 컨테이너가 멈추는 현상이 발생했습니다.
- **해결**: `docker-compose.yml`에서 DB 데이터 영역을 **RAM 기반 저장소(`tmpfs`)**로 설정하여 초기화 및 쓰기 성능을 비약적으로 향상시켰습니다.

### 데이터 정규화 vs 역정규화
- **고민**: 분석 테이블을 만들 때 ID만 저장할지, 모든 정보를 펼쳐서 저장할지 고민했습니다.
- **결정**: 분석 쿼리 속도를 높이기 위해 이벤트 발생 당시의 사용자 속성 정보를 포함하는 **역정규화(Denormalization)** 방식을 택해 Join 연산을 최소화했습니다.

---

## 🏗️ 4. 시스템 아키텍처
1. **Producer**: 가상의 이벤트를 생성하여 **Redis Streams**에 발행.
2. **Redis Streams**: 고성능 비동기 메시지 브로커 및 이벤트 저장소.
3. **Consumer**: 스트림을 구독하여 ETL(추출, 변환, 적재) 수행.

---

## ⚙️ 5. 환경 설정 (Ports)
- **Service DB**: `3307`
- **Analysis DB**: `3308`
- **Redis**: `6380`
- **Consumer App**: `8081`
- **Producer App**: `8080`
