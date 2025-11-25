# 5단계: 파일 관리 및 전략 패턴 (File Management)

이 단계에서는 파일 업로드/다운로드 기능을 구현하고, 전략 패턴(Strategy Pattern)을 통해 저장소(Local vs S3)를 유연하게 교체하는 방법을 학습합니다.

## 목표
- `FileService`와 `StorageStrategy`의 구조 이해.
- `MultipartFile`을 처리하는 파일 업로드 API 구현.
- 전략 패턴을 활용한 코드의 확장성 이해.

## 도메인 분석
`common` 모듈의 `file` 패키지를 분석하세요.
- `common/src/main/kotlin/com/pluxity/file/service/FileService.kt`
- `common/src/main/kotlin/com/pluxity/file/strategy/storage/StorageStrategy.kt`
- `common/src/main/kotlin/com/pluxity/file/strategy/storage/LocalStorageStrategy.kt`

**핵심 질문**:
1.  **전략 패턴**: `FileService`는 구체적인 저장 방식(Local/S3)을 어떻게 결정하나요?
2.  **파일 메타데이터**: 파일의 실제 데이터는 저장소에 저장되지만, 파일명, 크기, 경로 등의 메타데이터는 DB(`FileEntity`)에 저장됩니다. 이 흐름을 파악하세요.

## 실습 과제

### 과제 1: 파일 업로드 API 구현
1.  **브랜치 생성**: `onboarding/step-5-file` 브랜치를 생성하세요.
2.  **API 구현**: `core` 모듈에 파일을 업로드하고 ID를 반환하는 API를 만드세요.
    - Endpoint: `POST /api/v1/files`
    - Request: `MultipartFile`
    - Response: `FileResponse` (ID, URL 등 포함)
3.  **연동 테스트**:
    - Postman 등을 이용해 이미지를 업로드해보세요.
    - 로컬 저장소(`upload` 폴더 등)에 파일이 실제로 생성되는지 확인하세요.

### 과제 2: 건물 이미지 등록
앞서 만든 `Building` 또는 `Station` 생성 API를 수정하여, 업로드한 파일의 ID를 받아 썸네일로 등록하도록 만드세요.
- `StationCreateRequest`에 `thumbnailFileId` 필드를 추가하고, `StationService`에서 이를 처리하세요.

## 완료 조건
- [ ] 파일 업로드 시 로컬 디렉토리에 파일이 저장된다.
- [ ] DB에 파일 메타데이터가 저장된다.
- [ ] 전략 패턴을 통해 저장소 구현체를 쉽게 변경할 수 있음을 이해한다.
