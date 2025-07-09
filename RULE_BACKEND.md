# 📗 DuoPet 백엔드 개발 규칙 (RULE_BACKEND.md)

## 1. 📁 패키지 구조

```java
com.duopet/
┣ domain/
┃ ┣ board/
┃ ┃ ┣ controller/
┃ ┃ ┣ service/
┃ ┃ ┣ repository/       # JPA Repository 인터페이스
┃ ┃ ┗ dto/              # DTO, Entity
┃ ┣ member/
┃ ┃ ┣ controller/
┃ ┃ ┣ service/
┃ ┃ ┣ exception/
┃ ┃ ┣ repository/
┃ ┃ ┗ dto/
┃ ┣ notice/
┃ ┗ ...
┣ common/               # 공통 유틸, 페이징, 다운로드, 예외 코드 등
┣ config/               # 전역 설정 (CORS, WebMvc, QueryDSL 등)
┣ security/
┃ ┣ config/
┃ ┣ controller/
┃ ┣ provider/           # UserDetailsService, AuthProvider
┃ ┗ filter/             # JWTFilter, LoginFilter 등```

## 2. 📌 기본 개발 규칙

| 항목 | 규칙 |
|------|------|
| Controller | REST 방식, @RestController 사용, 응답은 ResponseEntity<T>로 감싸서 상태 코드와 함께 반환 |
| Service | 인터페이스 + 구현체 (ServiceImpl) 분리를 권장하되, 비즈니스 로직이 단순할 경우 생략 가능 |
| Repository | Spring Data JPA 기본 사용 + 복잡한 쿼리는 QueryDSL 사용을 우선 고려, @Query는 최후 수단 |
| Exception | 기능별 예외 클래스 생성, 공통 에러 코드는 common.exception에 정의, @RestControllerAdvice로 전역 처리 |
| DTO | 요청(Request)과 응답(Response)을 명확히 분리 (UserRequestDTO, UserResponseDTO). Entity는 Service 계층 밖으로 노출 금지 |
| Entity | @Entity, @Table, @Column 등 정확히 명시 + Lombok(@Getter, @Builder 등) 적극 활용. @Setter는 무분별한 사용 지양 |

## 3. ⚙️ 공통 컴포넌트 규칙

- `common/` 디렉토리에 다음 항목 모음:
  - `Search.java`, `Paging.java`: 게시판 공통 검색/페이징 도구
  - `FileDownloadView.java`: 첨부파일 다운로드 처리
- 필요 시 유틸 클래스는 `static` 메서드로 구성

## 4. 🔐 보안 및 인증

- JWT 인증 기반 구성
- 필터 구조:
  - `LoginFilter`: 로그인 시 토큰 발급
  - `JWTFilter`: 모든 요청에 대해 토큰 유효성 검사
  - `CustomLogoutHandler`: 로그아웃 시 Refresh 토큰 삭제
- 보안 관련 설정은 `security/config`에 집중 관리

## 5. 🧪 테스트 및 디버깅

- Controller 테스트는 `@WebMvcTest`, 통합 테스트는 `@SpringBootTest`
- 기본 단위 테스트 파일 위치: `src/test/java/org/myweb.first/{기능}`
- DB 연동 테스트 시 내장 DB(H2) 또는 Testcontainers 사용 권장

## 6. 🧩 커밋 메시지 예시

- `feat(member): 로그인 API 추가`
- `fix(board): 페이징 오류 수정`
- `refactor(notice): 응답 DTO 구조 개선`
- `test(common): 파일 다운로드 테스트 작성`
- 'docs(readme): API 명세 업데이트'

## 7. ✍️ 로깅(Logging) 규칙
# Log Level:
- INFO: 주요 비즈니스 흐름 (로그인 성공, 게시물 작성 등)
- DEBUG: 개발 단계에서 변수 확인 등 상세 정보
- WARN: 심각하진 않지만 잠재적 문제 상황 (e.g., 비정상적 파라미터 요청)
- ERROR: 예외 발생, DB 연결 실패 등 즉시 조치가 필요한 오류
- Log Format: [시간] [Thread] [Log Level] [Logger 이름] - 메시지 형식을 따름
- 주의: 개인정보(비밀번호, 주민번호 등)는 절대 로그에 남기지 않음

## 8. 💡 추가 팁

- Controller는 URL path 기준으로 기능 구분 (예: `/api/member`, `/api/notice`)
- Exception은 기능 단위로 나누되, `GlobalExceptionHandler` 통해 공통 처리 가능
- 각 계층 간 의존성은 명확히 (Controller → Service → Repository 순으로 흐름 제한)
