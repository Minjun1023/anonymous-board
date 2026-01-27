# 🎭 Anonymous Board (익명 게시판)

**Spring Boot & Thymeleaf 기반의 익명 커뮤니티 플랫폼**  
사용자 익명성을 보장하며, 실시간 소통과 안전한 커뮤니티 활동을 지원하는 웹 애플리케이션입니다.

## ✨ 주요 기능 (Key Features)

### 🔐 인증 및 보안 (Authentication & Security)
- **다양한 로그인 지원**: 이메일/비밀번호 로컬 로그인 및 OAuth2 소셜 로그인 (Google, Naver, Kakao)
- **JWT 기반 인증**: Access Token & Refresh Token을 활용한 보안
- **중복 로그인 감지 (Duplicate Login Detection)**:
  - 동일 계정으로 다른 기기/브라우저 접속 시 이전 세션 자동 로그아웃
  - Redis를 활용한 실시간 활성 토큰 관리 및 블랙리스트 처리
- **강력한 보안**: Spring Security 적용, 비밀번호 암호화 (BCrypt)

### 💬 실시간 소통 (Real-time Communication)
- **1:1 실시간 채팅**: WebSocket & STOMP 프로토콜 사용
- **Redis Pub/Sub**: 분산 서버 환경을 고려한 메시지 브로커 구조
- **읽음 확인 (Read Receipts)**: 상대방이 메시지를 읽었는지 실시간 확인
- **실시간 알림**: 새 메시지 수신 시 브라우저 알림 및 토스트 팝업

### 📝 게시판 (Community Board)
- **익명 게시글/댓글**: 자유로운 의견 개진 (작성자 익명성 보장)
- **투표 시스템**: 게시글 추천/비추천 (Like/Dislike)
- **설문조사 (Polls)**: 사용자 참여형 투표 기능
- **풍부한 에디터**: 텍스트 서식 지원

### 🎨 사용자 경험 (UX)
- **다크 모드 (Dark Mode)**: 눈의 피로를 줄이는 테마 변경 기능
- **반응형 디자인**: 모바일 및 데스크탑 환경 최적화
- **사용자 프로필**: 닉네임 변경, 프로필 이미지 설정
- **비밀번호 재설정**: 이메일 인증을 통한 안전한 재설정 프로세스

## 🛠 기술 스택 (Tech Stack)

### Backend
- **Java 17**, **Spring Boot 3.x**
- **Spring Security** (Auth & OAuth2)
- **Spring Data JPA** (Hibernate)
- **QueryDSL** (Complex Queries)
- **Redis** (Session, Caching, Pub/Sub)
- **MySQL** (Persistence)

### Frontend
- **Thymeleaf**, **HTML5**, **CSS3**
- **JavaScript (ES6+)**
- **Bootstrap 5** (UI Framework)
- **SockJS**, **STOMP** (WebSocket Client)

### DevOps & Tools
- **Gradle** (Build Tool)
- **Docker** (Optional, for Redis/DB)

## 🚀 시작하기 (Getting Started)

### 사전 요구사항 (Prerequisites)
- Java 17 이상
- MySQL 8.0 이상
- Redis

### 설치 및 실행 (Installation)

1. **레포지토리 클론**
   ```bash
   git clone https://github.com/your-username/anonymous-board.git
   cd anonymous-board
   ```

2. **환경 변수 설정 (`application.yml` or Environment Variables)**
   - DB, Redis 연결 정보 및 OAuth 클라이언트 ID 설정이 필요합니다.

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

4. **접속**
   - 브라우저에서 `http://localhost:8080` 접속

## 🔄 아키텍처 하이라이트: 중복 로그인 감지

이 프로젝트는 **Redis**를 활용하여 강력한 중복 로그인 감지 시스템을 구현했습니다.

1. **로그인 시**: `ActiveTokenService`가 사용자의 현재 토큰을 Redis에 저장 (`active_token:{email}`)
2. **중복 접속 시**: 
   - 새로운 로그인이 발생하면 기존에 저장된 토큰을 블랙리스트(Redis)로 이동 (`jwt:blacklist:{token}`)
   - 기존 사용자의 세션은 즉시 무효화됨
3. **검증 (Client-side)**:
   - 프론트엔드에서 주기적으로(30초) 또는 네비게이션 시 세션 유효성 검사
   - 무효화된 토큰 감지 시 즉시 자동 로그아웃 및 알림 표시

---
Developed by **Minjun** | 2026
