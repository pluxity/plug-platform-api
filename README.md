# PLUG-PLATFORM

## Language

- openjdk 21

## SpringBoot

- Springboot 3.4.1
- SpringSecurity

## build

- gradle-8.11.1

## Database

- local :h2(8082)
- stage : mariadb(3306)
- prod :mariadb(3306).

### ERD
- https://www.erdcloud.com/d/AQcTiBryStQxdpckZ

## Style Guide
DTO의 네이밍은 다음과 같이 기술합니다. (https://www.notion.so/plug-tf/DTO-6f34c761c4544d9a9d2277b864dcb276?pvs=4)

도메인 + 행위 + 계층

계층의 경우 Command, Condition, Request, Response 등이 존재합니다.

UserCreateRequest

UserUpdateRequest

UserResponse

와 같이 사용하시면 됩니다.

## Method 별 응답 규칙 컨벤션

GET : DataResponseBody<T>
POST : CreatedResponseBody<Long> or DataResponseBody<T>
PUT : ResponseBody
PATCH : DataResponseBody<T>
DELETE : void

## 참고문서


