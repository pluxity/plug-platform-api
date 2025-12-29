# 권한 체크 확장 작업 계획

## 현황 요약 (코드 기준)
- CheckPermission 정의: `common/src/main/kotlin/com/pluxity/global/annotation/CheckPermission.kt`
  - 파라미터: type(PermissionType), phase(PermissionCheckType), resourceType(ResourceType)
- AOP 처리: `common/src/main/kotlin/com/pluxity/global/aop/PermissionCheckAspect.kt`
  - `@Around`에서 `joinPoint.proceed()` 후 리턴 객체로 권한 판단
  - `PermissionStrategyResolver`로 전략 선택 후 `PermissionStrategy.check(user, resource)` 호출
  - `PermissionCheckType` 기준으로 SINGLE_ITEM/ITEM_LIST/FULL_ACCESS 처리
  - FULL_ACCESS는 `ResourceAllPermissible(resourceType)`로 `resourceId="ALL"` 체크
- 권한 판정 로직:
  - `PermissionStrategy`는 `Permissible(resourceType, resourceId)`를 받아 `User.canAccess` 호출
  - `User.canAccess`는 `Role.hasPermissionFor(resourceName, resourceId)`로 "정확히 일치"하는 권한만 허용
  - 권한 데이터는 `Permission(resourceName, resourceId)`로만 구성되어 레벨(READ/WRITE/ADMIN) 개념이 없음
- 현재 CheckPermission 사용 위치:
  - 조회성 메소드(예: `FacilityService.findById`, `findAll`) 위주
  - 등록/수정/삭제는 대부분 별도 권한 체크 없음

## 목표
- CheckPermission 기반 권한 체크를 조회(READ)뿐 아니라 등록/수정/삭제까지 확장
- 등록은 도메인 단위 권한 판단(리소스 전체에 대한 권한), 수정/삭제는 id 기반 권한 판단
- 권한 레벨: READ(조회), WRITE(조회+생성+수정), ADMIN(조회+생성+수정+삭제)
- 확정 정책: WRITE는 수정까지만 허용, 삭제는 ADMIN만 허용
- 수정/삭제는 역할(Role) 권한뿐 아니라 사용자 개별 단위 제한도 가능해야 함
- 도메인 단위 권한 제어는 별도 테이블 추가 없이 처리

## 변경 설계 (초안)
1) 권한 레벨 모델 추가
   - 새 enum (예: `PermissionLevel` 또는 `PermissionAction`) 도입
   - 레벨 포함 관계 정의: `ADMIN >= WRITE >= READ`
   - 저장 모델 확장: `Permission`에 레벨 컬럼 추가 또는 동등한 표현 방식 도입
     - 현재 `Permission(resourceName, resourceId)`만 있으므로 레벨 구분을 위한 스키마/DTO 변경 필요
   - 삭제 권한은 ADMIN만 허용하도록 비교 로직에 반영

2) 권한 생성/수정 API 확장
   - `PermissionRequest`(permission DTO)에 레벨 필드 추가
   - `PermissionGroupService.create/update`에서 레벨 저장 처리
   - 중복/유효성 체크 기준을 `(resourceName, resourceId, level)`로 조정

3) CheckPermission 어노테이션 확장
   - `CheckPermission`에 필수 권한 레벨 파라미터 추가 (예: `action = READ/WRITE/ADMIN`)
   - 등록/수정/삭제에서 필요한 레벨을 명시할 수 있도록 설계

4) PermissionCheckAspect 동작 확장
   - READ/WRITE/ADMIN 레벨 비교 로직 추가
   - 등록(생성): 리턴 객체가 없으므로 "실행 전" 도메인 단위 권한 체크가 필요
     - 도메인 단위 권한은 별도 테이블 없이 `resourceId="ALL"`로 표현
       - 기존 `ResourceAllPermissible(resourceType)` 재사용
       - 레벨 도입 시 `(resourceType, "ALL", level)` 비교가 되도록 판단 로직 확장
   - 수정/삭제: 기존 id 기반 체크 유지
     - 리턴 객체 기반 체크를 못 하는 경우(예: Unit 반환)에는 메소드 인자에서 id를 추출하거나
       기존 흐름처럼 내부에서 `findById` 호출 시 CheckPermission을 활용하는 방식 정리 필요
   - 사용자 개별 제한(수정/삭제):
     - 도메인 엔티티가 `BaseEntity`를 상속하고 있으므로 `createdBy`(Auditing) 활용 가능
     - 정책 옵션 예시:
       - ADMIN은 항상 허용
       - WRITE는 수정만 허용(삭제는 불가)
       - 본인 소유 리소스는 ADMIN으로 승급 처리(테이블 추가 없음)
         - 기준: `createdBy == currentUser`이면 ADMIN 권한으로 간주(삭제 허용)

5) 적용 지점 정리
   - 등록: 각 도메인 `save/create` 흐름에 `@CheckPermission(action=WRITE, resourceType=...)` 적용
   - 수정: `@CheckPermission(action=WRITE, type=ID, ...)` 또는 기존 `findById` 기반 체크 유지
   - 삭제: `@CheckPermission(action=ADMIN, type=ID, ...)` 적용 (WRITE는 삭제 불가)
   - 수정/삭제 시 사용자 개별 제한 적용 여부를 어노테이션 파라미터로 제어(예: `ownerOnly=true`)

6) 테스트/검증 시나리오
   - READ/WRITE/ADMIN별 허용/거부 케이스
   - 등록: 도메인 단위 권한 없을 때 거부
   - 수정/삭제: id 권한 없을 때 거부
   - FULL_ACCESS(ALL) 동작과 레벨 비교가 정상인지 확인
   - 수정/삭제: 작성자와 비작성자 케이스 구분 확인
