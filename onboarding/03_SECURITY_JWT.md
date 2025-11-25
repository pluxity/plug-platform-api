# 3단계: Spring Security & JWT (Security)

이 단계에서는 웹 애플리케이션 보안의 핵심인 인증(Authentication)과 인가(Authorization)가 어떻게 구현되어 있는지 학습합니다.

## 목표
- Spring Security의 필터 체인(`SecurityFilterChain`) 구조 이해.
- JWT(Json Web Token)의 구조와 발급/검증 로직 파악.
- Access Token과 Refresh Token을 이용한 인증 흐름 이해.

## 도메인 분석
다음 파일들을 집중적으로 분석하세요.
- `common/src/main/kotlin/com/pluxity/global/config/CommonSecurityConfig.kt`
- `common/src/main/kotlin/com/pluxity/authentication/security/JwtProvider.kt`
- `common/src/main/kotlin/com/pluxity/authentication/security/JwtAuthenticationFilter.kt`

**핵심 질문**:
1.  **필터 순서**: `JwtAuthenticationFilter`는 Spring Security의 기본 필터 체인 중 어디에 위치하나요? (`addFilterBefore` 확인)
2.  **토큰 검증**: `JwtProvider.isAccessTokenValid` 메소드는 토큰의 유효성을 어떻게 판단하나요? (만료 시간, 서명 검증 등)
3.  **인증 객체 생성**: 필터에서 토큰이 유효할 경우, `SecurityContextHolder`에 어떤 객체(`UsernamePasswordAuthenticationToken`)를 저장하나요?

## 실습 과제

### 과제 1: 토큰 발급 및 검증 테스트
1.  **브랜치 생성**: `onboarding/step-3-security` 브랜치를 생성하세요.
2.  **테스트 코드 작성**: `JwtProviderTest`를 생성하여 다음 시나리오를 검증하세요.
    - 유효한 사용자 정보로 Access Token 생성 시, 정상적으로 생성되어야 한다.
    - 생성된 토큰에서 `username`을 추출하면 원래 값과 일치해야 한다.
    - 만료된 토큰(만료 시간을 짧게 설정하여 테스트)을 검증하면 `ExpiredJwtException` 또는 `CustomException`이 발생해야 한다.

### 과제 2: 커스텀 필터 구현 (선택)
`JwtAuthenticationFilter`와 유사하게, 요청 헤더에 특정 키(`X-ONBOARDING-KEY`)가 있으면 로그를 남기는 간단한 필터를 만들어 `CommonSecurityConfig`에 등록해보세요.
- 필터가 정상적으로 동작하여 로그가 찍히는지 확인하세요.

### [Check Point]
단순히 토큰 생성뿐만 아니라, **잘못된 토큰(서명이 다른 토큰, 만료된 토큰)**으로 API를 호출했을 때 `401 Unauthorized`가 명확하게 리턴되는지, `GlobalExceptionHandler`가 동작하는지 확인하세요.
- `JwtAuthenticationFilter`에서 발생한 예외가 `ControllerAdvice`까지 도달하지 못하는 경우, `AuthenticationEntryPoint`를 어떻게 설정해야 하는지 고민해보세요.

## 완료 조건
- [ ] JWT의 구조(Header, Payload, Signature)를 설명할 수 있다.
- [ ] `JwtProvider`에 대한 단위 테스트가 통과한다.
- [ ] Spring Security 설정에서 특정 URL 패턴에 대한 접근 제어를 변경할 수 있다.
