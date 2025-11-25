# 4단계: 장비 및 시설 도메인 (Device & Facility)

이 단계에서는 물리적 자산을 관리하는 장비와 시설 도메인을 학습합니다.

## 목표
- `Device`, `Building`, `Facility` 엔티티의 상속 및 참조 관계 이해.
- 장비의 카테고리 변경 로직 및 연관관계 편의 메소드 이해.

## 도메인 분석
다음 파일들을 분석하세요.
- `common/src/main/kotlin/com/pluxity/device/entity/Device.kt`
- `core/src/main/kotlin/com/pluxity/building/Building.kt`
- `common/src/main/kotlin/com/pluxity/facility/Facility.kt`

**핵심 질문**:
- `Building`은 `Facility`를 상속받나요?
- `Device`는 `Building`에 직접 속하나요, 아니면 다른 방식으로 연결되나요? (힌트: `DeviceCategory`, `Feature` 등과의 관계 확인)
- `Device.changeCategory` 메소드에서 이전 카테고리와의 관계를 끊고 새 카테고리를 연결하는 로직을 확인하세요.

## 실습 과제

### 과제 1: 건물 및 장비 등록
1.  **브랜치 생성**: `onboarding/step-4-device` 브랜치를 생성하세요.
2.  **기능 구현**:
    - `Building`을 하나 생성하여 저장하는 코드를 작성하세요 (테스트 코드 또는 초기화 스크립트).
    - `Device`를 생성하고, 특정 `DeviceCategory`에 할당하는 로직을 구현하세요.

### 과제 2: 장비 카테고리 변경 검증
`Device`가 카테고리를 이동할 때, 객체 간의 참조가 올바르게 정리되는지 확인하는 테스트를 작성하세요.
- 시나리오:
    1. `CategoryA`, `CategoryB` 생성.
    2. `Device1`을 `CategoryA`에 할당.
    3. `Device1`의 카테고리를 `CategoryB`로 변경 (`changeCategory`).
    4. 검증: `CategoryA`의 장비 목록에는 `Device1`이 없어야 하고, `CategoryB`의 장비 목록에는 있어야 함.

### [심화 과제: 동시성 고민]
만약 Device의 카테고리를 변경하는 동시에, 다른 관리자가 해당 Device 정보를 수정한다면 어떻게 될까요?
- JPA의 `@Version`을 이용한 **낙관적 락(Optimistic Lock)**이 필요한 상황인지 고민해보고, 필요하다면 적용 후 리포트를 작성하세요.

## 완료 조건
- [ ] `Building`과 `Device` 엔티티의 관계를 설명할 수 있다.
- [ ] 카테고리 변경 시 연관관계가 정상적으로 업데이트됨을 테스트로 증명한다.
