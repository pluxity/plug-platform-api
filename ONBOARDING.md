# 플러그 플랫폼 API 온보딩 가이드

환영합니다! 이 프로젝트는 `plug-platform-api`의 구조와 핵심 도메인을 단계별로 학습할 수 있도록 구성되었습니다.
각 단계는 특정 도메인이나 기술적 주제에 집중하며, 실습 과제를 통해 코드베이스에 대한 이해를 높이는 것을 목표로 합니다.

## 사전 요구 사항 (Prerequisites)

시작하기 전에 다음 도구들이 설치되어 있는지 확인해주세요:
- **JDK 17+**: Kotlin 기반 프로젝트 빌드를 위해 필요합니다.
- **Docker & Docker Compose**: 데이터베이스 및 인프라 실행을 위해 필요합니다.
- **IntelliJ IDEA**: 권장 IDE (Kotlin 플러그인 포함).
- **Git**: 버전 관리를 위해 필요합니다.

## 온보딩 단계 (Steps)

아래 순서대로 진행해주세요. 각 단계의 링크를 클릭하면 상세 가이드로 이동합니다.

### 1. [프로젝트 설정 및 아키텍처 (Setup & Architecture)](./onboarding/01_SETUP.md)
- 프로젝트를 클론하고 실행 환경을 구축합니다.
- 멀티 모듈 구조(`common`, `core`, `collect`, `gs`)를 파악합니다.

### 2. [사용자 및 인증 도메인 (User & Authentication)](./onboarding/02_USER_DOMAIN.md)
- `User`, `Role`, `Permission` 엔티티를 분석합니다.
- 사용자 생성 및 권한 관리 로직을 실습합니다.

### 3. [심화: Spring Security & JWT (Security)](./onboarding/03_SECURITY_JWT.md)
- `JwtProvider`와 `JwtAuthenticationFilter`의 동작 원리를 이해합니다.
- Access Token과 Refresh Token의 발급 및 검증 과정을 분석합니다.

### 4. [장비 및 시설 도메인 (Device & Facility)](./onboarding/04_DEVICE_FACILITY.md)
- `Device`, `Building`, `Facility` 엔티티와 관계를 이해합니다.
- 장비 등록 및 시설 할당 프로세스를 학습합니다.

### 5. [파일 관리 및 전략 패턴 (File Management)](./onboarding/05_FILE_MANAGEMENT.md)
- `FileService`와 `StorageStrategy`를 분석합니다.
- 파일 업로드 API를 구현하고 전략 패턴의 이점을 학습합니다.

### 6. [비즈니스 로직 및 통합 (Business Logic)](./onboarding/06_BUSINESS_LOGIC.md)
- 도메인 간의 상호작용을 다룹니다.
- 권한 기반의 접근 제어 로직을 구현하고 테스트합니다.

### 7. [심화: 데이터 수집 및 비동기 처리 (Data Collection)](./onboarding/07_DATA_COLLECTION.md)
- `ClimateDataCollector`를 통해 외부 API 연동 및 코루틴(Coroutines) 활용법을 학습합니다.
- 토큰 관리 및 재시도 로직을 분석합니다.

### 8. [심화: 복합 도메인 및 트랜잭션 (Complex Domain)](./onboarding/08_COMPLEX_DOMAIN.md)
- `Station`, `Line` 등 복잡한 관계(N:M)를 가진 도메인을 다룹니다.
- 여러 서비스가 엮인 트랜잭션 처리(`StationService.save`)를 분석하고 구현합니다.

### 9. [종합: 실전 시나리오 구현 (Real-world Scenario)](./onboarding/09_ADVANCED_SCENARIO.md)
- 수집된 데이터와 도메인 로직을 결합하여 경보 시스템을 구축합니다.
- 실제 서비스 수준의 요구사항을 해결합니다.

---
**Tip**: 각 단계를 진행하면서 궁금한 점은 언제든지 시니어 개발자나 멘토에게 질문하세요. 화이팅!
