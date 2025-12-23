## 온보딩 메모

  - 레포 구조: 
    - auth/(Spring Boot 3 Kotlin, MySQL+JPA, RSA JWT auth/src/main/resources/private_key.pem, Rabbit 프로듀서)
    - gateway/(Spring Cloud Gateway, RSA 검증 gateway/src/main/resources/public_key.pem)
    - gorogoro-notification/(Spring Boot 메일+Rabbit 컨슈머)
    - 인프라 docker-compose.yml에 MySQL 3개·RabbitMQ 클러스터·앱 컨테이너 정의.
  - Auth 서비스: 
    - 엔드포인트 POST /api/auth/register|login|refresh
    - /api/users/modify|leave|info(X-User-Id 필요)
    - 내부 GET /server/users/nickname
    - User가 이메일/비밀번호/이름/닉네임 정책 검사, 회원가입 시 닉네임 랜덤 생성 후 Rabbit 환영 메일 이벤트
    - JWT RS256, 리프레시 토큰 DB 저장
    - SecurityConfig 전부 permitAll. // GateWay에서 체크중
  - Auth TODO/리스크: 
    - 이메일 인증 플로우 없음(sendVerificationEmail 미구현, INACTIVE만 존재)
    - Rabbit Consumer 주석 상태
    - UpdateUserRequest 검증 없음(이메일 중복 체크 누락, 비밀번호 필드 명/용도 혼동)
    - 리프레시 토큰 회수·회전 미지원(널 계약 불일치)
    - 테스트 거의 없음.
  - Gateway 서비스:
    - AuthorizationHeaderFilter가 RS256 검증 후 X-User-Id/X-User-Role 주입
    - CORS http://localhost:3000만 허용.
  - Gateway TODO: 
    - 공개키 동기화 확인·자동 리로드 검토
    - 역할 기반 접근 필요 시 확장
    - 401 응답 바디 표준화
    - 배포 도메인용 CORS 확장
    - 필터/라우트 로딩·헤더 전파 테스트 추가.
  - Notification 서비스: 
    - Rabbit notification.events.q 소비
    - SendGreetingMailEvent → 메일 전송(Gmail SMTP env 필요).
  - Notification TODO/이슈: 
    - SendVerifiedMailEvent 소비/메일 미구현
    - purchase.bindings 비어 있음 → 바인딩 정의
    - 프로듀서 TypeId와 컨슈머 매퍼 불일치 가능성
    - 에러 로깅·재처리 정책 미흡, 컨슈머/메일 어댑터 테스트 부족.
  - 공통 고려: 
    - JWT 키페어 동기화(auth 서명 ↔ gateway 검증)
    - 이벤트 스키마(TypeId, 버전) 문서화
    - 실행용 환경변수(.env)·포트 문서화
    - DB 마이그레이션/DDL 스크립트 정리 필요(현재 : JPA update 의존)

  ## 추천 다음 단계 TODO

  - Auth: 
    - 엑세스, 리프레시토큰 내려주는 방식 변경(프론트와 논의 필요 : All Cookie or Access Header & Refresf Cookie) 
    - 엔드포인트 보호 
    - 사용자 업데이트 입력 검증·중복 체크 추가.
    - 리프레시 토큰 1:1생성 보장 로직 구현
   
  - 이메일 인증: 
    - sendVerificationEmail 구현 
    - 인증 이벤트 처리
    - 휴면 → 활성화 로직 설계.
  - 메시징: 
    - Rabbit 소비자 활성화
    - 이벤트 계약(TypeId/스키마) 정합화
    - Notification 패키지/매퍼 수정
    - 유저 삭제 이벤트 발생시 이벤트 발송
  - Gateway: 
    - 라우트 키 검증·수정 
    - 필터/라우트 테스트 작성 
    - CORS 조정
    - 토큰 방식 변경시 검증 로직 수정
  - 품질: Auth/refresh/닉네임, Gateway 필터, Notification 컨슈머에 대한 테스트 추가, README와 환경변수 안내 작성.

  ## 자바 마이그레이션 체크리스트

  - 빌드: Kotlin 플러그인 제거, java/spring-boot 플러그인으로 전환, Java 17/21 설정, allOpen 등 Kotlin 전용 설정 삭제.
  - 모델/DTO: Kotlin data class → Java record 또는 Lombok(@Data/@Builder)로 변환, Bean Validation 어노테이션 유지.
  - 예외/패키지: Notification 예외 패키지 오타 수정 후 Java로 재작성, 공통 에러 응답/코드 인터페이스 통일.
  - 시큐리티/필터: Auth JWT 필터, Gateway AbstractGatewayFilterFactory를 Java로 구현; 람다 기반 구성은 명시적 클래스로 변환.
  - 로직 변환: apply/let 등 Kotlin 패턴을 명시적 null 체크/빌더로 대체, 컬렉션은 Java Streams 사용.
  - 테스트: JUnit5 유지, Kotlin 테스트를 Java로 포팅; 실제 메일 테스트는 환경변수 조건부 실행.
  - 문서/컨테이너: user-api.yml 최신화 후 Java 코드와 맞추기, Dockerfile(필요 시) 및 이미지 태그 갱신, 키 파일 경로·권한 확인.
  - 이벤트/직렬화: Kotlin 전용 어노테이션 제거, DTO/Envelope Java 버전으로 재작성, TypeId와 클래스 매퍼를 Java 클래스 이름에 맞게 조정.