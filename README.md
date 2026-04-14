# AWGDAS-v2

AWGDAS-v2는 Spring Boot 기반으로 개발 중인 프로젝트입니다.

현재 목표는 완성된 서비스 개발보다, GitHub Issue와 Claude Code를 활용해 기존 코드베이스에 기능을 추가하는 하네스 엔지니어링 워크플로우를 실험하는 것입니다.

## 현재 방향

- Java 기반 Spring Boot 프로젝트
- Thymeleaf 기반 서버 사이드 렌더링
- GitHub Issue 기반 작업 관리
- Claude Code 로컬 세션을 활용한 이슈 단위 개발
- PR 기반 리뷰 및 머지 흐름
- Flyway 기반 DB 스키마 변경 관리

## 기술 스택

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- Flyway
- H2
- PostgreSQL

## 개발 방식

이 프로젝트는 기능을 마일스톤 단위로 쌓기보다, GitHub Issue를 작업 단위로 사용합니다.

기본 흐름은 다음과 같습니다.

1. GitHub Issue 작성
2. 사람이 `agent-ready` 라벨 부여
3. Claude Code가 이슈 분석 및 작업 계획 작성
4. 승인 후 구현
5. 빌드/테스트 검증
6. PR 생성
7. 사람이 리뷰 후 머지

## 참고

현재 AWGDAS-v2는 운영 서비스 완성보다 하네스 엔지니어링과 에이전트 기반 개발 워크플로우를 실험하는 데 초점을 둡니다.
