# 6단계: 비즈니스 로직 및 통합 (Business Logic)

마지막 단계에서는 앞서 배운 도메인들을 결합하여 실제 비즈니스 요구사항을 처리하는 로직을 구현합니다.

## 목표
- 권한(`Permission`) 기반의 접근 제어 로직 구현.
- `User`의 `canAccess` 메소드 활용.
- 복잡한 시나리오에 대한 통합 테스트 작성.

## 도메인 분석
`User.kt`의 `canAccess` 메소드를 다시 자세히 살펴보세요.
```kotlin
fun canAccess(resourceName: String, resourceId: String): Boolean =
    userRoles.any { it.role.name == RoleType.ADMIN.roleName } ||
    userRoles.any { it.role.hasPermissionFor(resourceName, resourceId) }
```
이 메소드는 사용자가 특정 리소스(예: 특정 카테고리의 장비들)에 접근 권한이 있는지 판단합니다.

## 실습 과제

### 과제 1: 장비 제어 권한 체크 서비스 구현
1.  **브랜치 생성**: `onboarding/step-6-logic` 브랜치를 생성하세요.
2.  **서비스 구현**: `DeviceService` (또는 유사 서비스)에 `controlDevice(userId: Long, deviceId: String)` 메소드를 구현하세요.
    - 로직:
        1. `userId`로 `User` 조회.
        2. `deviceId`로 `Device` 조회.
        3. `User.canAccess(ResourceType.DEVICE_CATEGORY.name, device.category.id)`를 호출하여 권한 확인.
        4. 권한이 없으면 `AccessDeniedException` 발생.
        5. 권한이 있으면 "Device Controlled" 로그 출력 또는 더미 동작 수행.

### 과제 2: 통합 테스트
위 시나리오를 검증하는 테스트를 작성하세요.
- **Case 1**: ADMIN 권한을 가진 유저는 모든 장비를 제어할 수 있다.
- **Case 2**: 특정 카테고리에 대한 권한만 가진 유저는 해당 카테고리의 장비만 제어할 수 있다.
- **Case 3**: 권한이 없는 유저는 예외가 발생해야 한다.

## 완료 조건
- [ ] 권한 체크 로직이 포함된 서비스가 구현되었다.
- [ ] 다양한 권한 시나리오에 대한 테스트 케이스가 모두 통과한다.

---
**축하합니다!** 모든 온보딩 단계를 완료했습니다. 이제 프로젝트의 실무 작업을 진행할 준비가 되었습니다.
