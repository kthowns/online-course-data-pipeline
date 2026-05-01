# 🎓 실시간 강의 구매 패턴 분석 데이터 파이프라인
> **Analyzing Course Purchase Patterns & Identifying Pain Points**

이 프로젝트는 온라인 강의 플랫폼에서 발생하는 다양한 사용자 행동을 실시간으로 수집, 가공, 적재하고 시각화하는 **엔드투엔드 실시간 데이터 파이프라인**입니다. **"도커만 있다면 어디서든 명령어 한 줄로 구동"** 가능한 완전한 이식성을 제공합니다.

---

## 🚀 1. 핵심 특징 (Key Features)

- **Zero-Configuration**: 로컬에 Java, Gradle, Python 등을 설치할 필요가 없습니다. 모든 빌드와 실행 환경이 Docker 내부에 캡슐화되어 있습니다.
- **Multi-stage Build**: Docker 내부에서 소스 코드를 직접 빌드하여 실행 파일을 생성합니다.
- **Physical Isolation**: 운영계(Service)와 분석계(Analysis) DB를 물리적으로 분리하여 대규모 분석 쿼리 시에도 서비스 안정성을 보장합니다.
- **High Performance**: RAM 기반 저장소(`tmpfs`)를 활용하여 로컬 환경에서도 빠른 DB 초기화와 I/O 성능을 제공합니다.
- **Real-time Visualization**: Streamlit 기반의 대시보드를 통해 1초 이내의 지연시간으로 데이터 인사이트를 제공합니다.

---

## 🏃 2. 실행 방법

### 필요한 도구
- **Docker Desktop** (또는 Docker Engine & Compose)

### 실행 명령어 (단 한 줄로 시작)
```powershell
docker-compose up --build -d
```
> **참고**: 최초 실행 시 MariaDB 데이터베이스 초기화 및 Java 소스 빌드로 인해 약 2~3분 정도 소요될 수 있습니다. 시스템이 안정화되면 대시보드 접속이 가능해집니다.

---

## 📊 3. 실시간 분석 대시보드
브라우저에서 실시간으로 업데이트되는 데이터 분석 지표를 확인할 수 있습니다.

- **접속 주소**: [http://localhost:8501](http://localhost:8501)
- **주요 분석 지표**:
  1. **결제 퍼널 분석**: 장바구니 → 결제 시도 → 구매 완료 과정의 이탈률 시각화
  2. **연령대별 인기 카테고리**: 세대별 선호 강의 분야 분석
  3. **플랫폼별 결제 에러 현황**: 기기 환경(Web/Mobile)별 장애 비중 추적
  4. **실시간 트래픽 추이**: 시간 흐름에 따른 사용자 행동 빈도 분석

---

## 🏗️ 4. 스키마 명세 (Schema Specification)

### 설계 이유
운영계(Service DB)의 무결성을 유지하면서도, 분석계(Analysis DB)에 **나이, 성별, 플랫폼 등**의 다차원 데이터를 이벤트 시점에 스냅샷으로 함께 적재하도록 설계했습니다. 이를 통해 운영 DB에 부하를 주지 않고 복잡한 집계 쿼리를 독립적으로 수행할 수 있습니다.

### [Service DB] - 운영 마스터 데이터 (Port: 3307)

#### 1. `users` (사용자 정보)
| 컬럼명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | 사용자 고유 ID (Auto Increment) |
| `email` | Varchar(255) | 사용자 이메일 주소 |
| `name` | Varchar(255) | 사용자 이름 |
| `role` | Enum | 역할 (STUDENT, TEACHER) |
| `gender` | Enum | 성별 (MALE, FEMALE) |
| `age` | Integer | 사용자 나이 |
| `createdAt` | DateTime | 계정 생성 일시 |

#### 2. `courses` (강의 정보)
| 컬럼명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | 강의 고유 ID (Auto Increment) |
| `teacher` | Varchar(255) | 담당 강사명 |
| `level` | Enum | 강의 난이도 (HIGH, MIDDLE, LOW) |
| `category` | Varchar(255) | 강의 카테고리 (예: Programming) |
| `price` | BigInt | 강의 가격 |
| `totalDuration` | Integer | 전체 강의 시간 (초 단위) |
| `createdAt` | DateTime | 강의 등록 일시 |

#### 3. `payments` (결제 마스터)
| 컬럼명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | 결제 고유 ID (Auto Increment) |
| `userId` | BigInt | 결제 요청 사용자 ID |
| `courseId` | BigInt | 결제 대상 강의 ID |
| `status` | Enum | 결제 상태 (ADD_TO_CART, PURCHASE 등) |
| `createdAt` | DateTime | 최초 생성 시각 |
| `updatedAt` | DateTime | 마지막 상태 변경 시각 |

---

### [Analysis DB] - 분석 및 로그 데이터 (Port: 3308)

#### 1. `raw_event_logs` (원본 이벤트 수집)
| 컬럼명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | 로그 고유 ID (Auto Increment) |
| `data` | Text (JSON) | 수집된 원본 EventEnvelope 전체 데이터 |
| `createdAt` | DateTime | 수집된 시각 |

#### 2. `payment_analysis` (결제 퍼널 분석용)
| 컬럼명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | 분석 데이터 고유 ID (Auto Increment) |
| `logId` | BigInt | `raw_event_logs` 참조용 ID |
| `userId` | BigInt | 사용자 ID (이벤트 시점) |
| `courseId` | BigInt | 강의 ID (이벤트 시점) |
| `status` | Enum | 결제 단계 상태 |
| `amount` | BigInt | 결제 요청 금액 |
| `category` | Varchar(255) | 강의 카테고리 (Snapshotted) |
| `platform` | Varchar(255) | 사용 기기 (WEB, MOBILE) |
| `region` | Varchar(255) | 접속 지역 (예: KR) |
| `age` | Integer | 사용자 나이 (Snapshotted) |
| `gender` | Enum | 사용자 성별 (Snapshotted) |
| `eventTime` | DateTime | 실제 이벤트 발생 시각 |
| `errorCode` | Varchar(255) | 결제 실패 시 에러 코드 |

#### 3. `search_analysis` (검색 패턴 분석용)
| 컬럼명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | 분석 데이터 고유 ID (Auto Increment) |
| `logId` | BigInt | `raw_event_logs` 참조용 ID |
| `userId` | BigInt | 사용자 ID |
| `keyword` | Varchar(255) | 검색 키워드 |
| `resultCount` | Integer | 검색 결과 개수 |
| `viewedCourseId` | BigInt | 검색 후 클릭한 강의 ID |
| `age` | Integer | 사용자 나이 |
| `gender` | Enum | 사용자 성별 |
| `eventTime` | DateTime | 이벤트 발생 시각 |

---

## 🤔 5. 구현하면서 고민한 점

### MariaDB 전환과 권한 설정
- **문제**: MySQL 8.0이 로컬 디스크 환경에서 초기화 속도가 매우 느려 컨테이너가 멈추는 현상이 잦았습니다.
- **해결**: 더 가볍고 빠른 MariaDB 10.11로 전환하고, `MARIADB_ROOT_HOST` 설정을 통해 컨테이너 간의 DB 접속 권한 문제를 해결했습니다.

### 데이터 휘발성과 성능의 트레이드오프
- **고민**: 로컬 환경에서 빠른 테스트를 위해 데이터를 디스크 대신 RAM(`tmpfs`)에 저장하도록 설정했습니다.
- **결과**: 데이터 영구 저장은 포기했지만, 대신 어느 PC에서든 막힘없는 빌드와 실행 속도를 확보했습니다.

### 분석 쿼리용 데이터 역정규화
- **결정**: 분석 성능을 극대화하기 위해 Join 연산을 줄이고, 이벤트 발생 시점의 사용자 속성(나이, 성별 등)을 테이블에 직접 포함하는 역정규화 방식을 채택했습니다.

---

## ⚙️ 6. 환경 설정 (Ports)
- **Analytics Dashboard**: `8501` (접속용)
- **Service DB**: `3307`
- **Analysis DB**: `3308`
- **Redis**: `6380`
- **Producer / Consumer App**: `8080`, `8081`
