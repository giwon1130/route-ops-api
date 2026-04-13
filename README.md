# RouteOps API

`RouteOps API`는 호출형 이동 서비스 운영 콘솔을 위한 백엔드다.  
실운영 데이터가 아직 없더라도 운영 화면과 의사결정 흐름을 검증할 수 있도록, 시나리오 기반 시뮬레이션 데이터를 제공한다.

## Purpose

이 API는 단순 CRUD 서버가 아니다.  
운영 콘솔이 필요한 데이터를 한 번에 제공하는 `운영 시뮬레이션 + 대시보드 API` 역할을 한다.

핵심 목적은 다음과 같다.

- 권역 수요와 차량 상태를 운영자 관점으로 표현
- 현재 운영 지표와 위험 신호를 요약
- 재배치 추천과 근거를 제공
- 30분 단기 수요 예측을 제공
- 시간 흐름에 따라 운영 시나리오를 바꿔보며 화면을 검증

## Stack

- Kotlin 1.9
- Spring Boot 3
- Gradle
- JUnit 5

## What It Returns

### 대시보드 데이터
- KPI summary
- zones
- vehicles
- demands
- alerts
- metric timeseries

### 운영 지원 데이터
- rebalancing recommendations
- demand forecasts
- scenario status
- recommendation rule basis

### 상세 분석 데이터
- vehicle histories
- zone timelines
- operations report

### 시뮬레이션 제어
- tick
- reset

## Main Endpoints

- `GET /api/v1/dashboard/snapshot`
- `GET /api/v1/dashboard/summary`
- `GET /api/v1/zones`
- `GET /api/v1/vehicles`
- `GET /api/v1/demands`
- `GET /api/v1/alerts`
- `GET /api/v1/metrics/timeseries`
- `GET /api/v1/recommendations`
- `GET /api/v1/forecasts`
- `POST /api/v1/simulation/tick`
- `POST /api/v1/simulation/reset`

## Snapshot Response Includes

`/api/v1/dashboard/snapshot` 하나로 프론트가 운영 콘솔을 렌더링할 수 있게 구성했다.

- `summary`
- `zones`
- `vehicles`
- `demands`
- `alerts`
- `metrics`
- `recommendations`
- `forecasts`
- `scenario`
- `vehicleHistories`
- `zoneTimelines`
- `report`

## Run Local

```bash
./gradlew bootRun
```

- default port: `8087`

## Test

```bash
./gradlew test
```

## Docker

```bash
docker build -t route-ops-api .
docker run -p 8087:8087 route-ops-api
```

통합 실행이 필요하면 상위 묶음 디렉토리의 compose도 사용할 수 있다.

```bash
cd /Users/g/workspace/public/mobility/route-ops
docker compose up --build
```

- compose 기준 web: `http://localhost:4174`
- compose 기준 API health: `http://localhost:8087/actuator/health`
- 컨테이너 이미지에는 healthcheck용 `wget`을 포함했다.

## Project Structure

```text
src/main/kotlin/com/giwon/routeops
├── bootstrap/
├── common/
└── features/dashboard/
    ├── application/
    ├── domain/
    └── presentation/
```

## Domain Overview

- `Zone`: 운영 권역
- `Vehicle`: 차량 상태와 위치
- `Demand`: 호출 요청
- `OperationAlert`: 운영 경고
- `RebalancingRecommendation`: 차량 재배치 추천
- `DemandForecast`: 단기 수요 예측
- `VehicleHistory`: 차량 최근 이동 이력
- `ZoneTimeline`: 권역 변화 흐름
- `OperationsReport`: 운영 요약 리포트

## Current Limitations

- 실제 차량 GPS 데이터는 아님
- 실제 배차 엔진 연동 없음
- 예측은 머신러닝 모델이 아니라 시나리오 기반 계산
- 추천은 규칙 기반 로직 중심

## Next Extensions

- PostgreSQL/PostGIS 연결
- Redis/SSE 실시간 갱신
- 규칙 기반 추천 고도화
- What-if 시뮬레이션 API
- AI 운영 브리핑 API
