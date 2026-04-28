---
roadmap-id: product
status: active
last-updated: 2026-04-29
---

# product — AWGDAS 제품 본체 (게임 자동 생성 웹앱)

소개 페이지 / 로그인 / 관리자 / 게임 자동 생성(5-에이전트) / 갤러리 등 사용자 대상 웹 애플리케이션 자체의 발전 항목. 하네스 자체 발전(harness.md)과는 분리.

## 항목

### RM-PRODUCT-001: 게임 자동 생성 (5-에이전트 + 협업 가시화)

- status: planned
- epic: true
- 설명: PL/기획/디자인/개발/QA 5종 에이전트의 비선형 메시지 패싱 기반 게임 자동 생성 시스템. 공용 게임 명세 + 명시적 fan-out + 순환 가드 + 사용자 가시화까지 포함하는 큰 비전.

### RM-PRODUCT-002: 공용 게임 명세 도메인 모델

- status: planned
- parent: RM-PRODUCT-001
- blocks: [RM-PRODUCT-003]
- 설명: 5 에이전트가 공유하는 단일 게임 명세 도메인. 모든 에이전트가 읽고/쓰는 진실의 원천. 동시 쓰기 정책(잠금/버전) 포함. 메시지에 컨텍스트를 다 싣지 않기 위한 토대.

### RM-PRODUCT-003: 에이전트 메시지 패싱 인프라

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-002]
- blocks: [RM-PRODUCT-004, RM-PRODUCT-005, RM-PRODUCT-006, RM-PRODUCT-007, RM-PRODUCT-008, RM-PRODUCT-010, RM-PRODUCT-011, RM-PRODUCT-012]
- 설명: 송신 에이전트가 직접 다음 수신자 선택. 명시적 fan-out(여러 에이전트 동시 요청) + 결과 수신자 명시. 순환 가드: 전체 턴 한도 + 동일 페어 연속 N회 알람 → 도달 시 PL로 강제 라우팅.

### RM-PRODUCT-004: PL 에이전트

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- blocks: [RM-PRODUCT-009]
- 설명: 사용자 ↔ 시스템 인터페이스(요청 직후 1.5-shot 첫 요약 후 confirm), 종료 선언, 에이전트 충돌 중재. 일반 메시지 라우팅은 각 에이전트가 직접 (PL이 병목 안 됨).

### RM-PRODUCT-005: 기획 에이전트

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- 설명: 게임 룰/플로우/난이도/콘텐츠 기획. 공용 명세 문서의 기획 섹션 작성/수정.

### RM-PRODUCT-006: 디자인 에이전트

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- 설명: 게임 화면 UI/UX + 비주얼/아트(스프라이트, 색상, 타일). 공용 명세 문서의 디자인 섹션 작성/수정.

### RM-PRODUCT-007: 개발 에이전트

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- 설명: 웹게임 코드 생성 + 빌드/패키징(개발 단계 마지막). 공용 명세를 입력으로 실행 가능한 산출물 생산.

### RM-PRODUCT-008: QA 에이전트

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- 설명: 산출물 동작 검증, 명세 위반/버그 식별, 수정 요청 메시지 생성. 통과 시 종료 후보 시그널을 PL에게.

### RM-PRODUCT-009: 게임 생성 요청 입력 UX

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-004]
- 설명: 자유 서술 원샷 입력 폼. 제출 시 PL 에이전트가 받아 "이렇게 이해했음" 첫 요약 후 사용자 confirm 1번으로 시작.

### RM-PRODUCT-010: 요청 상태/내역 조회

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- 설명: 본인의 게임 생성 요청 진행 상태(현재 어떤 에이전트가 작업 중인지) + 과거 요청 내역 조회. 실패 사유 표시 포함.

### RM-PRODUCT-011: 실패 분류 + 정책

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- 설명: 실패 분류(시스템성 = 네트워크/외부 API 오류 vs 사용자/논리 실패). 시스템성만 자동 할당량 복구. 모호 케이스는 보수적으로 차감 유지. 자동 재시도 X. 실패 사유는 사용자에게 표시.

### RM-PRODUCT-012: 에이전트 협업 가시화 UI

- status: planned
- parent: RM-PRODUCT-001
- depends-on: [RM-PRODUCT-003]
- 설명: 메시지 흐름과 공용 명세 변경을 사용자에게 보여주는 타임라인/스트림 뷰. 어떤 에이전트가 누구에게 무엇을 요청했고 fan-out 어떻게 분기됐는지 가시화. 프로젝트 차별점.

### RM-PRODUCT-013: 게임 결과물 활용

- status: planned
- epic: true
- 설명: 완성된 웹게임을 사용자가 활용하는 경로 — 다운로드, 브라우저 플레이, 공유 링크, 공개 갤러리. Epic A의 산출물에 가치 부여.

### RM-PRODUCT-014: 산출물 저장 + 다운로드

- status: planned
- parent: RM-PRODUCT-013
- depends-on: [RM-PRODUCT-007]
- blocks: [RM-PRODUCT-015]
- 설명: 개발 에이전트가 생산한 웹게임 번들을 영속 저장. 사용자가 본인 게임을 다운로드(zip 등) 가능. 저장 경로/포맷 정의.

### RM-PRODUCT-015: 브라우저 직접 플레이

- status: planned
- parent: RM-PRODUCT-013
- depends-on: [RM-PRODUCT-014]
- blocks: [RM-PRODUCT-016]
- 설명: 다운로드 없이 본인 게임을 브라우저에서 바로 실행. 014에서 저장한 산출물을 정적 호스팅 또는 iframe으로 로드.

### RM-PRODUCT-016: 공유 링크 + 공개 토글

- status: planned
- parent: RM-PRODUCT-013
- depends-on: [RM-PRODUCT-015]
- blocks: [RM-PRODUCT-017]
- 설명: 게임별 공유 가능 URL 생성. 공개 on/off 토글이 접근 제어와 연결. 토글 라벨에 "공개 시 갤러리에 노출됩니다" 명시 (별도 동의 절차 없이 토글 자체가 동의).

### RM-PRODUCT-017: 공개 갤러리

- status: planned
- parent: RM-PRODUCT-013
- depends-on: [RM-PRODUCT-016]
- 설명: 공개 on된 게임 목록 페이지. 표시 항목: 썸네일, 제목, 생성 일시, 마스킹 계정(예: `jo***`). 요청 원문/설명은 노출 X. 비로그인 방문자도 접근 가능 (소개 페이지와 함께 공개 영역).

### RM-PRODUCT-018: 공개 소개 페이지

- status: planned
- 설명: 비로그인 방문자가 보는 공개 소개 페이지. 프로젝트 컨셉/주요 기능 안내. CTA는 로그인 하나만 (셀프 가입 없음). 공개 갤러리(RM-PRODUCT-017)와 더불어 비로그인 영역의 일부.

### RM-PRODUCT-019: 로그인 + 초기 관리자 계정

- status: done
- completed-at: 2026-04-29
- related-issues: [#15]
- blocks: [RM-PRODUCT-020]
- 설명: Spring Security 기본 설정 + Flyway seed로 초기 관리자 계정 1명. 로그인 페이지 + 인증 후 관리자/일반 사용자 라우팅 분기. 셀프 가입 없음.

### RM-PRODUCT-020: 관리자 — 하위 계정 CRUD

- status: planned
- depends-on: [RM-PRODUCT-019]
- blocks: [RM-PRODUCT-021]
- 설명: 관리자 페이지에서 하위 계정 생성/비밀번호 랜덤 초기화/비활성화/삭제. 셀프 가입 없으니 모든 일반 사용자는 여기서 발급.

### RM-PRODUCT-021: 할당량 모델 + 관리자 조정

- status: planned
- depends-on: [RM-PRODUCT-020]
- 설명: 계정별 게임 생성 요청 할당량(기본 10) 모델. 관리자가 백오피스에서 특정 사용자 할당량 +1 수동 보정. 시스템성 실패 시 자동 복구(RM-PRODUCT-011)와 별개의 운영용 안전망.

### RM-PRODUCT-022: 인앱 알림 뱃지

- status: planned
- depends-on: [RM-PRODUCT-010]
- 설명: 게임 생성 완료/실패 시 사용자에게 인앱 뱃지로 알림. 이메일/브라우저 노티 X (사용자가 명시 거부 — 토큰 비용/거부율 우려).

### RM-PRODUCT-023: 로컬 H2 파일 모드 전환

- status: done
- completed-at: 2026-04-29
- blocks: [RM-PRODUCT-020, RM-PRODUCT-021]
- related-issues: [#17]
- 설명: 로컬 개발용 H2를 in-memory(`jdbc:h2:mem:`)에서 파일 모드(`jdbc:h2:file:`)로 전환. 재시작 후에도 관리자/하위 계정/할당량 등 영속 데이터가 유지되도록. `secrets-local.yaml`의 URL 변경 + 생성되는 DB 파일 경로(예: `data/awgdas-local.*`)를 `.gitignore`에 추가. Flyway 영향: 정상 동작이지만, 메모리 모드와 달리 이미 적용된 migration 파일을 수정하면 checksum mismatch로 부팅 실패 — `database-rules.md`에 "기존 V 수정 금지(새 V 추가만), 어쩔 수 없이 수정 시 로컬 DB 파일 삭제" 안내 추가.
