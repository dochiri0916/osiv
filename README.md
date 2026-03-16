# OSIV

JPA OSIV(`open-in-view`) on/off와 식별자 참조(`getReferenceById`)를 비교해, 읽기/쓰기 경로의 쿼리 예측 가능성을 검증한 프로젝트입니다.
여기에는 실행 가능한 코드와 핵심 요약만 두고, 설계 배경과 선택 이유는 블로그에 정리했습니다.

## 실험 범위

- `open-in-view=true/false` 프로필로 웹 계층 지연 로딩 경계 비교
- 읽기 경로 비교: 연관 탐색(`GET /api/osiv/posts`) vs 식별자 참조 조회(`GET /api/osiv/posts-id-reference`)
- 쓰기 경로 비교: 엔티티 조회 후 생성(`POST /api/osiv/comments/load`) vs 식별자 참조 후 생성(`POST /api/osiv/comments/id-reference`)
- SQL 계측으로 쓰기 경로 선조회 비용 비교(`SELECT + INSERT` vs `INSERT`)

## 자세한 내용

[N+1을 디버깅으로 버티지 않기 위해 OSIV를 끄고 읽기/쓰기를 분리한 이유](https://velog.io/@dochiri0916/N1%EC%9D%84-%EB%94%94%EB%B2%84%EA%B9%85%EC%9C%BC%EB%A1%9C-%EB%B2%84%ED%8B%B0%EC%A7%80-%EC%95%8A%EA%B8%B0-%EC%9C%84%ED%95%B4-OSIV%EB%A5%BC-%EB%81%84%EA%B3%A0-%EC%9D%BD%EA%B8%B0%EC%93%B0%EA%B8%B0%EB%A5%BC-%EB%B6%84%EB%A6%AC%ED%95%9C-%EC%9D%B4%EC%9C%A0)
