# RouteOps API

호출형 이동 서비스 운영자를 위한 운영 콘솔 API다. 초기 버전은 실운영 데이터 대신 시뮬레이션 데이터를 사용해 권역 수요, 차량 상태, 배차 지연, 운영 추천 흐름을 검증하는 MVP다.

## MVP 제공 기능
- 대시보드 요약 KPI 조회
- 권역, 차량, 호출, 알림, 시계열 지표 조회
- 권역 재배치 추천 조회
- 30분 수요 예측 조회
- 시간대 운영 시나리오 상태 포함
- 추천별 규칙 근거(rule basis) 포함
- 차량 이동 이력 데이터 포함
- 권역 타임라인 데이터 포함
- 운영 리포트 요약 포함
- 운영 시뮬레이션 `tick` / `reset`

## 실행
```bash
./gradlew bootRun
```

기본 포트는 `8087`이다.

## Docker 실행
```bash
docker build -t route-ops-api .
docker run -p 8087:8087 route-ops-api
```

## 주요 엔드포인트
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
