# 2단계: 사용자 및 인증 도메인 (User & Authentication)

이 단계에서는 플랫폼의 가장 기본이 되는 사용자와 권한 시스템을 이해합니다.

## 목표
- `User`, `Role`, `UserRole` 엔티티 구조 및 관계 이해.
- 사용자 생성 및 정보 수정 로직 파악.
- 비밀번호 변경 정책 및 로직 검증.

## 도메인 분석
다음 파일들을 열어 코드를 분석해보세요.
- `common/src/main/kotlin/com/pluxity/user/entity/User.kt`
- `common/src/main/kotlin/com/pluxity/user/entity/Role.kt` (또는 관련 파일)
- `common/src/main/kotlin/com/pluxity/user/entity/UserRole.kt`

**핵심 질문**:
- `User`와 `Role`은 어떤 관계인가요? (1:N, N:M?)
- `User.changePassword` 메소드는 어떤 부가 작업을 수행하나요? (예: `lastPasswordChangeDate` 업데이트)
- `User.addRole` 시 중복 체크는 어떻게 이루어지나요?

## 실습 과제

### 과제 1: 사용자 생성 API 구현
1.  **브랜치 생성**: `onboarding/step-2-user` 브랜치를 생성하세요.
2.  **기능 구현**: `core` 모듈에서 관리자(ADMIN) 권한을 가진 사용자를 생성하는 API를 구현하세요.
    - Endpoint: `POST /api/v1/onboarding/users`
    - Request Body: `username`, `password`, `name`
    - 로직:
        - `User` 엔티티 생성.
        - `RoleRepository`에서 `ADMIN` 롤 조회.
        - `User.addRole()`을 통해 롤 할당.
        - `UserRepository.save()`로 저장.

### 과제 2: 비밀번호 변경 테스트
`User` 엔티티의 비밀번호 변경 로직을 검증하는 단위 테스트를 작성하세요.
- 위치: `common/src/test/kotlin/com/pluxity/user/entity/UserTest.kt` (없으면 생성)
- 테스트 케이스:
    - 비밀번호 변경 시 `password` 필드가 변경되어야 한다.
    - 비밀번호 변경 시 `lastPasswordChangeDate`가 현재 시간으로 갱신되어야 한다.

## 완료 조건
- [ ] 관리자 권한을 가진 유저가 정상적으로 생성되고 DB에 저장된다.
- [ ] `User` 엔티티에 대한 단위 테스트가 통과한다.
