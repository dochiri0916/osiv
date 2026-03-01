# OSIV Study Project

JPA OSIV(`open-in-view`) on/off 차이와 식별자 참조 방식을 실습하는 프로젝트입니다.

자세한 배경과 설계 기준은 블로그 글에서 확인하세요.

- 블로그 원문: [BLOG.md](./BLOG.md)

## 실행

OSIV 활성화:

```bash
./gradlew bootRun --args='--spring.profiles.active=osiv-on'
```

OSIV 비활성화:

```bash
./gradlew bootRun --args='--spring.profiles.active=osiv-off'
```

## API 확인

```bash
curl -i http://localhost:8080/api/osiv/posts
curl -i http://localhost:8080/api/osiv/posts-id-reference

curl -i -X POST http://localhost:8080/api/osiv/comments/load \
  -H "Content-Type: application/json" \
  -d '{"postId":1,"content":"entity load command"}'

curl -i -X POST http://localhost:8080/api/osiv/comments/id-reference \
  -H "Content-Type: application/json" \
  -d '{"postId":1,"content":"id reference command"}'
```

## 테스트

```bash
./gradlew test
```
