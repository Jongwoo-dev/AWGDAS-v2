# Roadmap Index

이 디렉토리는 AWGDAS-v2의 **목표/로드맵을 영역별로 분할** 보관한다. 자동 이슈 제안 (`/issue-suggest`)이 활성 로드맵을 읽고 다음 작업 후보를 추출한다.

## 사용 규칙

- 로드맵은 **여러 개 공존 가능** (예: 하네스 / 백엔드 / 게임 콘텐츠).
- 본 INDEX는 항상 로드되는 진입점 — 짧게 유지.
- 로드맵 파일 추가/제거 시 이 INDEX의 표를 갱신.
- 항목은 **삭제 금지**, `status: deprecated`로 마킹만. 정리는 `/roadmap-cleanup`이 별도로 처리.

## 활성 로드맵

| 파일 | status | 한 줄 요약 |
|------|--------|-----------|
| [harness.md](harness.md) | active | 자동 개발 하네스 자체 발전 (부분 스캔, 자동 이슈, 자동 검증 등) |
| [product.md](product.md) | active | AWGDAS 제품 본체 (게임 자동 생성 웹앱) |

## ID 부여 규칙

- 형식: `RM-{ROADMAP}-{NNN}`
  - ROADMAP: 로드맵 파일명을 대문자로 (예: `harness.md` → `HARNESS`)
  - NNN: 3자리 일련번호, 로드맵 파일별로 독립적으로 증가
- ID는 **불변** — 항목이 deprecated 되어도 ID는 보존 (이슈/상태 문서가 참조하기 때문)
- 새 항목 추가 시 해당 로드맵의 최대 번호 + 1

## 항목 상태값

- `planned` — 시작 전, 의존성 충족 시 후보가 됨
- `in-progress` — 진행 중
- `done` — 완료
- `deprecated` — 폐기, 참조는 유지 (cleanup 시점까지)
- `blocked` — 외부 차단 요인, 후보에서 제외

## 의존 관계 선언 (`depends-on` / `blocks`)

항목 간 선후 관계는 두 필드 중 어느 쪽으로든 선언할 수 있다.

- `depends-on: [RM-...]` — 이 항목이 시작되려면 먼저 done 되어야 하는 항목들
- `blocks: [RM-...]` — 이 항목이 done 되어야 시작 가능한 항목들 (`depends-on`의 dual)

**규칙:**
- 두 필드는 같은 관계의 두 방향. 한쪽만 적어도 `/issue-suggest`는 두 필드를 합쳐 양방향 그래프를 만든다.
- 새 항목 추가 시 (`/roadmap-add`)는 양쪽을 모두 묻고 누락을 줄인다. 양쪽에 중복 선언해도 무방.
- `/issue-suggest`의 후보 우선순위는 양방향 그래프의 **역참조 수**(이 항목을 가리키는 다른 planned 항목 수)를 핵심 신호로 사용. 역참조 수가 많을수록 foundational로 간주되어 위로 올라온다.

**예시 (RM-003 → RM-004 관계를 두 방향으로 표현):**

```markdown
### RM-HARNESS-003: 워크플로우 검증
- depends-on: [RM-HARNESS-001, RM-HARNESS-002]
- blocks: [RM-HARNESS-004]

### RM-HARNESS-004: 비용/품질 메트릭
- depends-on: [RM-HARNESS-003]   # 위와 같은 관계를 반대 방향으로도 선언 (선택, 안전망)
```

## Epic / 자식 항목

큰 비전(여러 PR로 나뉠 수밖에 없는 단위)도 로드맵에 보존되어야 한다. 이를 위해 두 필드를 사용한다.

- `epic: true` — 이 항목은 추상 비전이며, **`/issue-suggest`의 후보에서 제외**된다. 자식 항목만 후보가 된다.
- `parent: RM-{ID}` — 이 항목이 어떤 epic의 자식인지 명시.

**규칙:**
- epic의 모든 자식이 `done`이면, epic도 자동으로 `done`이 된다 (`/issue-suggest`가 처리).
- epic의 모든 자식이 `archived`/`deprecated`이면, epic도 `/roadmap-cleanup`의 archive 후보가 된다.
- epic 자체에 `depends-on`을 둘 수 있다. 자식 항목은 별도로 `depends-on`을 가질 수 있다.
- 자식 항목은 자체적인 RM ID를 가진다 (예: `RM-HARNESS-005`가 epic이면 자식은 `RM-HARNESS-006`, `RM-HARNESS-007` 등 — 부모-자식 관계는 ID 자체가 아니라 `parent` 필드로만 표현).
- epic 안에 epic을 두는 다단계는 **금지** (단순화).

**예시:**

```markdown
### RM-HARNESS-010: 회원 시스템 전체

- status: planned
- epic: true
- 설명: 가입/로그인/탈퇴/프로필을 포함한 전체 회원 기능

### RM-HARNESS-011: 회원 가입 플로우

- status: planned
- parent: RM-HARNESS-010
- 설명: 이메일/비밀번호 가입, 검증 메일 발송

### RM-HARNESS-012: DB 기반 인증으로 전환

- status: planned
- parent: RM-HARNESS-010
- depends-on: [RM-HARNESS-011]
```

## 신규 로드맵 추가 시

1. `docs/roadmap/{name}.md` 생성 (frontmatter 포함)
2. 본 INDEX의 "활성 로드맵" 표에 1줄 추가
3. 첫 항목은 `RM-{NAME}-001`부터
