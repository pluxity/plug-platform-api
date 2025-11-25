# 7단계: 데이터 수집 및 비동기 처리 (Data Collection)

이 단계에서는 외부 시스템과 통신하여 데이터를 수집하고, 이를 효율적으로 처리하는 고급 기술을 학습합니다.

## 목표
- `WebClient`를 이용한 외부 API 호출 이해.
- Kotlin `Coroutines`를 활용한 비동기/병렬 처리 학습.
- 토큰 기반 인증 및 갱신 로직, 재시도(Retry) 패턴 분석.

## 도메인 분석
`collect` 모듈의 `ClimateDataCollector.kt` 파일을 심층 분석하세요.

**핵심 포인트**:
1.  **비동기 병렬 처리**: `supervisorScope`와 `launch`를 사용하여 여러 디바이스의 데이터를 동시에 수집하는 방식을 이해하세요.
2.  **Thread Safety**: `Mutex`를 사용하여 토큰 갱신 시 동시성 문제를 어떻게 해결했는지 확인하세요.
3.  **에러 핸들링**: `400 Token Incorrect` 에러 발생 시 토큰을 갱신하고 재시도하는 흐름(`callClimateDataWithRetry`)을 따라가 보세요.

## 실습 과제

### 과제 1: Mock 서버 연동 수집기 구현
1.  **브랜치 생성**: `onboarding/step-7-collection` 브랜치를 생성하세요.
2.  **Mock API 구성**: (선택) Postman Mock Server 등을 이용해 간단한 JSON을 반환하는 API를 준비하거나, 기존 코드를 모방하여 가상의 URL을 호출하도록 설정하세요.
3.  **Collector 구현**:
    - `WebClient`를 설정하여 외부 API를 호출하는 `SimpleCollector`를 만드세요.
    - `runBlocking` 대신 `suspend` 함수와 `coroutineScope`를 사용하여 5개의 가상 디바이스 데이터를 병렬로 가져오는 로직을 작성하세요.
    - 수집된 데이터를 DB에 저장하세요.

### 과제 2: 재시도 로직 구현
네트워크 불안정 상황을 가정하여 재시도 로직을 구현하세요.
- API 호출 실패 시 최대 3회까지 재시도하도록 구현하세요.
- 재시도 간격에 지수 백오프(Exponential Backoff)를 적용해보세요 (예: 1초 -> 2초 -> 4초).

## 완료 조건
- [ ] `WebClient`와 `Coroutines`를 사용하여 병렬로 데이터를 수집할 수 있다.
- [ ] `Mutex`의 역할과 필요성을 설명할 수 있다.
- [ ] API 호출 실패 시 재시도 로직이 정상 동작함을 로그로 확인할 수 있다.
