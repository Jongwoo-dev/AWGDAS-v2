# GitHub API Retry Policy

하네스 명령(`/issue-suggest`, `/issue-start` 등)이 호출하는 `gh` CLI는 일시 실패할 수 있다. 본 문서는 그 경우 어떻게 대응할지를 규정한다.

본 정책은 **dogfooding cycle 001 Finding 5**(`gh issue create`가 HTTP 504로 한 차례 실패 → 목록 조회로 미생성 확인 후 재실행하여 성공)에서 도출되었다. 추가 데이터 포인트가 쌓이면 본 정책은 재검토된다.

## 일시 실패 vs. 영구 실패

**일시 실패 (재시도 대상)**
- HTTP 5xx (500/502/503/504 등)
- 네트워크 타임아웃, 연결 리셋, DNS 일시 오류

**영구 실패 (재시도 금지)**
- HTTP 4xx (401/403/404/422 등) — 권한/인증/리소스 부재 같은 클라이언트 측 영구 오류
- 명령 문법 오류
- HTTP 429 (rate limit) — `gh` CLI 자체가 backoff 처리하므로 명령 측 재시도 불필요

stderr에 `504`, `timeout`, `connection reset` 같은 패턴이 보이면 일시 실패로 간주한다. 명확하지 않으면 사용자에게 surface한다.

## 단일 룰

> **일시 실패 시 5초 대기 후 1회 재시도.** mutation 호출은 재시도 전에 idempotency check를 먼저 수행한다. 재시도도 실패하면 사용자에게 surface한다.

재시도 횟수와 백오프를 더 정교하게 만들고 싶더라도, 추가 dogfooding 데이터가 나오기 전까지는 단일 룰을 유지한다.

## 호출 분류

### 안전한 호출 (idempotency check 불필요)

읽기 전용이라 재실행해도 부작용 없는 호출:

- `gh issue list`, `gh issue view`
- `gh pr list`, `gh pr view`
- `gh label list`

→ 일시 실패 시 5초 대기 후 그대로 1회 재시도.

### Mutation 호출 (idempotency check 필수)

상태를 바꾸는 호출:

- `gh issue create`, `gh pr create`
- `gh issue edit --add-label` / `--remove-label`, `gh pr edit`
- `gh label create`

→ 일시 실패 시 **재시도 전에** "이전 시도가 실제로는 성공했는지" 먼저 확인한다. 504 같은 일시 오류는 서버가 요청을 처리한 뒤 응답만 실패한 경우도 있어, 무작정 재시도하면 중복 생성 / 라벨 토글 오작동이 발생할 수 있다.

## Idempotency check 절차

### `gh issue create` / `gh pr create`

같은 제목으로 이슈/PR이 이미 있는지 검색한다.

```
gh issue list --state all --search "in:title \"{TITLE}\"" --json number,title --limit 5
```

- 매칭이 있고 제목이 정확히 일치하면 → 이전 시도가 성공한 것으로 간주, 재시도 생략하고 해당 번호 사용.
- 매칭이 없으면 → 5초 대기 후 재시도.

**False-positive 위험.** GitHub 검색은 부분 매칭과 closed 이슈도 포함하므로, 동명의 과거 이슈가 잘못 매칭될 수 있다. 매칭 결과가 애매하면(여러 건 매칭, 닫힌 이슈가 섞임 등) 재시도하지 말고 사용자에게 결과를 surface한 뒤 수동 확인을 요청한다.

### `gh issue edit --add-label` / `--remove-label`

대상 이슈의 현재 라벨 상태를 조회해서 이미 적용/해제되었는지 확인한다.

```
gh issue view {number} --json labels --jq '.labels[].name'
```

- `--add-label X` 재시도 전: X가 이미 라벨 목록에 있으면 → 이전 시도 성공, 재시도 생략.
- `--remove-label X` 재시도 전: X가 라벨 목록에 없으면 → 이전 시도 성공, 재시도 생략.

### 그 외 mutation

`gh pr edit`, `gh label create` 등도 같은 원리로 "현재 상태 조회 → 이미 반영되었으면 skip, 아니면 재시도". 호출별로 무엇을 조회해야 멱등성이 확인되는지는 호출 시점에 판단한다.

## 재시도가 또 실패하면

2번째 시도도 실패한 경우:

- 명령 절차를 그 자리에서 중단한다.
- 사용자에게 다음을 surface한다:
  - 어떤 호출이 실패했는가
  - stderr 마지막 줄
  - 멱등성 확인 결과 (예: "동명의 이슈가 이미 있는데 closed 상태")
  - 권장 다음 액션 (예: "수동으로 `gh issue list`를 확인하고 다시 시도")
- 자동으로 더 시도하지 않는다.

## 적용 범위

본 정책은 `.claude/commands/*.md`의 `gh` 호출에 적용된다. 현재는 다음 두 명령에서 명시적으로 참조한다:

- `.claude/commands/issue-suggest.md`
- `.claude/commands/issue-start.md`

다른 명령(`/issue-pr`, `/roadmap-cleanup` 등)은 추후 dogfooding cycle에서 같은 패턴이 surface되면 본 정책을 동일하게 적용한다.
