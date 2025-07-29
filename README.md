# PLUG-PLATFORM

## Language

- openjdk 21

## SpringBoot

- Springboot 3.4.1
- SpringSecurity

## build

- gradle-8.11.1

## Database

- local: h2(8082)
- stage: mariadb(3306)
- prod: mariadb(3306).

### ERD

- 

### 데이터베이스 초기 설정: facility 테이블 인덱스
**중요:** 프로젝트를 새로 구축할 때 `facility` 테이블에 대해 다음 인덱스를 수동으로 생성해야 합니다.

#### 배경
Facility 테이블은 데이터를 물리적으로 삭제하는 대신, `deleted` 플래그를 `true`로 변경하여 비활성화하는 '소프트 삭제(Soft Delete)' 방식을 사용합니다. `facility` 테이블의 `code`(시설 코드)와 `name`(시설명)은 삭제되지 않은(활성) 데이터 내에서 유일성이 보장되어야 합니다.

JPA/Hibernate의 표준 Annotation만으로는 `where` 조건이 포함된 **부분 인덱스(Partial Index)** 생성을 지원하지 않으므로, 애플리케이션 실행 전 아래 스크립트를 수동으로 실행해야 합니다.

#### 적용 방법
`facility` 테이블 생성 후, 아래 SQL 구문을 데이터베이스에서 직접 실행하여 주십시오.

```sql
-- 'deleted = false'인 행에 대해서만 code의 유일성을 보장합니다.
create unique index facility_code_unique_if_not_deleted
    on facility (code)
    where (deleted = false);

-- 'deleted = false'인 행에 대해서만 name의 유일성을 보장합니다.
create unique index facility_name_unique_if_not_deleted
    on facility (name)
    where (deleted = false);
```

## 기본 컨벤션
- https://github.com/pluxity/plug-platform-api/wiki

## Method 별 응답 규칙 컨벤션
- GET: DataResponseBody<T>
- POST: Void 
- PUT: Void
- PATCH: Void
- DELETE: Void

## 참고문서


