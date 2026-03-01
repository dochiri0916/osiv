# 연관관계는 편한데, 왜 식별자 참조를 선택하게 됐는가

JPA를 처음 쓸 때는 연관관계를 잘 맺는 것이 정석이라고 믿었다.  
객체 탐색이 편하고, 필요한 데이터도 자연스럽게 따라올 것 같았다.

하지만 운영 구간에서 반복해서 마주친 문제는 비슷했다.

- 어디서 쿼리가 추가로 실행되는지 예측이 어렵다.
- 화면 요구가 늘어날수록 연관 탐색 범위가 넓어진다.
- 성능 이슈가 나면 요청 로그를 뒤져서 원인을 찾는 식이 된다.

그래서 질문을 다시 잡았다.

- 연관관계 매핑은 언제 이점이 큰가?
- 식별자 참조는 언제 비용을 줄이는가?
- N+1은 어디에서 통제해야 하는가?

이 글은 현재 프로젝트(`osiv`)에서 직접 실험한 결과를 기준으로 정리한 기준이다.

## 문제를 재현한 방식

프로젝트는 같은 도메인에서 읽기 경로를 두 가지로 나눴다.

1. 연관관계 탐색 경로: `GET /api/osiv/posts`
2. 식별자 참조 조회 경로: `GET /api/osiv/posts-id-reference`

쓰기 경로도 두 가지로 나눴다.

1. 엔티티 조회 후 생성: `POST /api/osiv/comments/load`
2. 식별자 참조 후 생성: `POST /api/osiv/comments/id-reference`

핵심 설정은 다음과 같다.

```yaml
spring:
  jpa:
    open-in-view: false
```

## N+1의 본질: 연관관계 자체가 아니라 읽기 계획

N+1은 보통 읽기에서 터진다.  
루트 목록을 가져온 뒤, 연관 데이터를 지연 로딩으로 건드리는 순간 쿼리가 누적된다.

핵심은 단순하다.

- "연관관계를 맺었는지"보다
- "읽기 시점에 로딩 계획을 명시했는지"가 더 중요하다.

현재 프로젝트도 이 점을 그대로 보여준다.

- `posts` 경로는 컨트롤러에서 `post.getComments()`를 접근한다.
- `posts-id-reference` 경로는 서비스에서 필요한 데이터를 먼저 수집해 DTO로 반환한다.

결론적으로 연관관계를 유지해도, 읽기 계획을 명시하지 않으면 같은 문제가 반복된다.

## OSIV의 장점과 경계 혼선

OSIV가 켜져 있으면 웹 계층에서도 지연 로딩이 가능하다.  
초기 개발 속도는 분명히 좋아진다.

하지만 트레이드오프도 분명하다.

- 컨트롤러/직렬화 단계에서 쿼리가 발생할 수 있다.
- 쿼리 책임 경계가 서비스 밖으로 밀린다.
- 성능 이슈 재현 지점이 흐려진다.

그래서 이 프로젝트는 OSIV를 끄고(`open-in-view=false`) 경계를 먼저 고정했다.

## 식별자 참조의 실제 이점: 쓰기 경로 조회 감소

식별자 참조는 "연관 대상의 상세 상태가 당장 필요 없는 쓰기"에서 가장 효율적이다.

이 프로젝트의 쓰기 메서드는 다음 두 가지다.

```java
@Transactional
public Long createCommentByEntityLoad(Long postId, String content) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
    Comment comment = new Comment(content, post);
    commentRepository.save(comment);
    return comment.getId();
}

@Transactional
public Long createCommentByIdReference(Long postId, String content) {
    Post postIdReference = postRepository.getReferenceById(postId);
    Comment comment = new Comment(content, postIdReference);
    commentRepository.saveAndFlush(comment);
    return comment.getId();
}
```

`p6spy`로 SQL 개수를 측정한 결과는 다음과 같다.

- `comments/load`(`findById`): `SELECT + INSERT` -> `2`개
- `comments/id-reference`(`getReferenceById`): `INSERT` -> `1`개

실제 테스트 출력:

```text
SQL count comparison | entity-load=2, id-reference=1
```

즉, 식별자 참조의 핵심 가치는  
"쓰기에서 불필요한 선조회(부모 엔티티 조회)를 줄이는 것"이다.

## 중요한 단서: 식별자 참조만으로 N+1이 해결되지는 않는다

이 점은 분리해서 봐야 한다.

- 식별자 참조의 주효과: 쓰기 경로 조회 최소화
- N+1 통제의 핵심: 읽기 경로 로딩 계획 명시

읽기에서 엔티티 그래프 탐색을 열어두면 N+1은 언제든 다시 나온다.  
그래서 읽기는 DTO 중심 조회와 전용 조회 리포지토리로 계획을 고정해야 한다.

## 언제 조회를 생략하면 안 되는가

식별자 참조를 쓸 때도 예외가 있다. 다음 경우는 즉시 조회/검증이 필요하다.

- 존재 여부를 즉시 보장해야 하는 경우
- 권한/소유권 검증이 필요한 경우
- 연관 대상 현재 상태에 따라 비즈니스 규칙이 달라지는 경우

즉, 식별자 참조는 만능 규칙이 아니라 유스케이스 선택지다.

## 적용 기준

잘 맞는 경우:

- 조회 트래픽이 크고 화면별 조회 요구가 자주 바뀌는 서비스
- N+1이나 불필요 쿼리 이슈가 반복되는 팀
- API 응답 모델과 도메인 모델을 분리하려는 경우

과할 수 있는 경우:

- 조회 패턴이 고정된 단순 내부 시스템
- 팀이 아직 JPA 기본 규칙을 정착하는 단계라 분리 비용이 큰 경우

## 정리

이 프로젝트에서 얻은 결론은 다음이다.

1. OSIV를 꺼서 웹 계층 지연 로딩 경로를 닫는다.
2. 쓰기에서는 식별자 참조로 불필요 조회를 줄인다.
3. 읽기에서는 반환 모델을 먼저 정하고 로딩 계획을 명시한다.

한 줄로 요약하면,

"식별자 참조를 쓰면 N+1이 자동 해결되는 것이 아니라, 읽기와 쓰기 책임을 분리할 때 비로소 N+1을 통제할 수 있다."

## 실험에 사용한 엔드포인트

```text
GET  /api/osiv/posts
GET  /api/osiv/posts-id-reference
POST /api/osiv/comments/load
POST /api/osiv/comments/id-reference
```

## 실험 재현 명령

```bash
./gradlew test --tests "com.dochiri.osiv.OsivOffIntegrationTest.comparesSqlCountBetweenEntityLoadAndIdReference" --info
```

## 참고

- Spring Boot 설정: `spring.jpa.open-in-view`
- Spring Data JPA: `JpaRepository#getReferenceById`
- Jakarta Persistence: `EntityManager#getReference`
- Jakarta Persistence: `EntityNotFoundException`
- Hibernate User Guide: Fetching
