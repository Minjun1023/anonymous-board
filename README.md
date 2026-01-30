# 익명 게시판 (Anonymous Board)

익명으로 자유롭게 소통하는 커뮤니티 플랫폼 🗨️
[프로젝트 GitHub 💻](https://github.com/Minjun1023/anonymous-board)
<br><br>

## 프로젝트 미리보기🧚

- 메인 페이지
<img src="![alt text](image.png)">

- 게시판 목록
<img src="https://via.placeholder.com/800x400.png?text=Board+List+Screenshot">

- 게시글 상세
<img src="https://via.placeholder.com/800x400.png?text=Post+Detail+Screenshot">
<br><br>

## 프로젝트 소개📄

**� 프로젝트 기간 : 2025.12.15 ~ 2026.01.29**

익명 게시판은 사용자들이 닉네임으로 자유롭게 의견을 나눌 수 있는 익명 커뮤니티 플랫폼입니다.

**주요 특징**
- 🎭 **완벽한 익명성**: 닉네임 기반 소통으로 개인정보 보호
- 📢 **다양한 게시판**: 공지, 자유, 비밀, QnA, 핫 게시판 제공
- 💬 **실시간 채팅**: WebSocket 기반 1:1 채팅 기능
- 🔐 **소셜 로그인**: Google, Kakao, Naver 간편 로그인
- 🌙 **다크모드**: 사용자 편의를 위한 테마 전환 기능
- 📱 **반응형 디자인**: 모든 디바이스에서 최적화된 UI
<br><br>

## 프로젝트 기술 스택💻

#### 📌 프로그래밍 언어 및 프레임워크
<img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
<img src="https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white">

#### 🎨 UI 스타일링
<img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white">
<img src="https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white">
<img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black">

#### �️ 데이터베이스 및 캐시
<img src="https://img.shields.io/badge/PostgreSQL-13-4169E1?style=for-the-badge&logo=postgresql&logoColor=white">
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">

#### 🌐 빌드 & 배포
<img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
<br><br>

## 프로젝트 주요 기능⚙️

### 1. 게시판 시스템 📝
- **다중 게시판 지원**
	- 📢 공지사항: 관리자 전용 공지 게시판
	- 🗨️ 자유게시판: 일반적인 주제로 자유로운 소통
	- 🔒 비밀게시판: 민감한 내용 공유
	- ❓ QnA게시판: 질문과 답변 전용 공간
	- 🔥 핫 게시판: 인기 게시글 자동 집계 (좋아요-싫어요 10이상)

### 2. 게시글 작성 및 상호작용 ✍️
- **게시글 CRUD**
	- 게시판별 게시글 작성, 수정, 삭제
	- 마크다운 에디터 지원
	- 게시판 타입 선택 가능
- **상호작용 기능**
	- 좋아요/싫어요 기능
	- 댓글 작성 및 답글
	- 조회수 자동 증가

### 3. 사용자 인증 및 관리 🔐
- **소셜 로그인 (OAuth2)**
	- Google, Kakao, Naver 계정으로 간편 로그인
	- JWT 토큰 기반 인증
- **이메일 인증**
	- 회원가입 시 이메일 인증
	- 비밀번호 찾기 (인증 코드 발송)
- **프로필 관리**
	- 닉네임, 비밀번호 변경
	- 프로필 이미지 업로드
	- 작성한 게시글 및 댓글 조회

### 4. 실시간 기능 💬
- **1:1 채팅**
	- WebSocket(STOMP) 기반 실시간 채팅
	- 읽음 표시 및 타이핑 인디케이터
	- 채팅방 목록 및 히스토리
- **실시간 알림**
	- 새 댓글, 좋아요 알림
	- 채팅 메시지 알림

### 5. 보안 및 세션 관리 🛡️
- **다중 로그인 방지**
	- Redis 기반 세션 관리
	- 중복 로그인 시 기존 세션 자동 종료
- **보안 기능**
	- CSRF 보호
	- XSS 방지
	- BCrypt 비밀번호 암호화

### 6. 기타 편의 기능 ⚙️
- **다크모드**
	- localStorage 기반 테마 저장
	- 실시간 테마 전환
- **검색 및 필터링**
	- 게시판별 필터링
	- 키워드 검색
	- 조회순/댓글순 정렬
- **문의 시스템**
	- 사용자 문의 제출
	- 관리자 답변 기능
<br><br>

## 트러블 슈팅🧑💻

### [드롭다운 메뉴 클릭 이슈 해결](https://github.com/user/repo/issues/1)

**[요약]**<br>
문제 ▶️ "내 정보" 드롭다운 메뉴를 클릭해도 열리지 않는 문제 발생
<br>해결 ▶️ 세션 체크 이벤트 리스너에서 드롭다운 토글 버튼을 CSS 셀렉터 레벨에서 제외 (`.nav-link:not(.dropdown-toggle)`)
<br>교훈 ▶️ Bootstrap의 `data-bs-toggle` 속성과 커스텀 이벤트 리스너가 충돌할 수 있으므로, CSS 셀렉터를 정확하게 지정하여 의도하지 않은 이벤트 바인딩 방지 필요

### [Bootstrap 중복 로드로 인한 충돌](https://github.com/user/repo/issues/2)

**[요약]**<br>
문제 ▶️ `my-inquiries.html` 페이지에서만 드롭다운 메뉴가 작동하지 않음
<br>해결 ▶️ 페이지별로 Bootstrap JS를 중복 로드하지 않고 `layout.html` fragment만 사용하도록 통일
<br>교훈 ▶️ SPA가 아닌 전통적인 템플릿 구조에서도 리소스 중복 로드를 주의해야 하며, fragment를 활용한 일관된 레이아웃 관리 중요

### [게시판 타입 분리 및 기본값 설정](https://github.com/user/repo/issues/3)

**[요약]**<br>
문제 ▶️ 새로운 게시판 타입 추가 시 기존 게시글의 `boardType`이 null로 남아 에러 발생
<br>해결 ▶️ `Post` 엔티티에 `@ColumnDefault("'FREE'")` 추가 및 DTO에 기본값 설정
<br>교훈 ▶️ DB 스키마 변경 시 기존 데이터와의 호환성을 고려하여 기본값 설정 및 마이그레이션 전략 필요
<br><br>

## 프로젝트 후기✍️

### 기술적 성과 🎯
이번 프로젝트를 통해 Spring Boot의 핵심 개념들을 실전에 적용하며 깊이 있게 학습할 수 있었습니다. 특히 **Spring Security와 OAuth2를 활용한 인증/인가**, **JPA를 통한 효율적인 데이터 관리**, **Redis를 활용한 세션 관리 및 캐싱**을 구현하면서 실무에 필요한 기술 스택을 익힐 수 있었습니다.

### 도전과 성장 🚀
**WebSocket을 이용한 실시간 채팅 구현**은 가장 큰 도전이었습니다. STOMP 프로토콜을 처음 다루면서 메시지 브로커의 동작 방식을 이해하고, 읽음 처리와 타이핑 인디케이터 같은 세부 기능까지 완성하면서 실시간 통신에 대한 이해도를 높일 수 있었습니다.

또한 **다중 로그인 방지 및 세션 관리** 기능을 구현하며 보안의 중요성을 체감했습니다. Redis를 활용한 세션 저장소 구성과 중복 로그인 감지 로직을 통해 엔터프라이즈급 애플리케이션의 보안 요구사항을 이해하게 되었습니다.

### UX/UI 개선 경험 🎨
단순한 기능 구현을 넘어 **사용자 경험을 개선하는 과정**이 인상 깊었습니다. 드롭다운 메뉴 충돌 문제를 해결하면서 프론트엔드와 백엔드의 상호작용을 더 깊이 이해하게 되었고, Bootstrap과 커스텀 JavaScript의 조화로운 사용법을 익혔습니다. 다크모드, 반응형 디자인 등을 구현하며 사용자 중심 개발의 가치를 배웠습니다.

### 아쉬운 점과 개선 방향 📈
- **테스트 코드 부족**: 시간 관계상 충분한 단위 테스트와 통합 테스트를 작성하지 못한 점이 아쉽습니다. 향후 JUnit과 Mockito를 활용한 체계적인 테스트 작성 예정입니다.
- **성능 최적화**: N+1 쿼리 문제나 페이지네이션 최적화 등 성능 개선 여지가 있습니다. QueryDSL 도입 및 인덱싱 전략 수립이 필요합니다.
- **CI/CD 파이프라인**: 현재 수동 배포 방식에서 GitHub Actions를 활용한 자동화된 배포 파이프라인 구축이 필요합니다.

### 배운 점 및 느낀 점 💡
프로젝트를 진행하며 **문제 해결 능력**이 크게 향상되었습니다. 드롭다운 메뉴 이슈처럼 예상치 못한 문제에 직면했을 때, 단계적으로 원인을 분석하고 해결하는 과정에서 디버깅 스킬과 문제 해결 방법론을 체득했습니다.

또한 **코드의 재사용성과 유지보수성**의 중요성을 깨달았습니다. Thymeleaf fragment를 활용한 레이아웃 통일, DTO 패턴을 통한 계층 분리 등 클린 코드 원칙을 실천하며 확장 가능한 애플리케이션 구조를 만드는 법을 배웠습니다.

무엇보다 **사용자 피드백을 반영하는 애자일한 개발 프로세스**를 경험한 것이 값졌습니다. 게시판 타입 추가, 네비게이션 개선 등 실제 사용자 요구사항을 즉각적으로 반영하면서 빠른 이터레이션의 중요성을 체감했습니다.

이번 프로젝트는 단순한 CRUD를 넘어 **실무에 가까운 풀스택 개발 경험**을 제공했으며, 앞으로 더 나은 개발자로 성장하는 발판이 되었습니다. 🚀
<br><br>

---

© 2026 익명 게시판. All rights reserved.
