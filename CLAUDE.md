# CLAUDE.md

## Java 클래스 헤더 명세

새로 생성하는 모든 Java 클래스 파일은 package 선언 바로 아래, class 선언 바로 위에 아래 형식의 헤더를 넣는다.

```java
/**
 * @since       yyyy.mm.dd
 * @author      preah
 * @description class name camel case
 **********************************************************************************************************************/
```

- `@since` : 파일 생성일 (`yyyy.mm.dd`)
- `@author` : 항상 `preah` 고정
- `@description` : 클래스명(PascalCase)을 단어 경계로 쪼개 소문자 + 공백으로 표기
  - 예: `DoclingRestClientConfiguration` → `docling rest client configuration`
  - 예: `DoclingService` → `docling service`

기존 파일에는 소급 적용하지 않는다. 새로 만드는 파일에만 적용.

## YAML 정렬 규칙

`application*.yaml` 등 설정 yml 작성/수정 시, 같은 depth 에 있는 scalar 값 형제 키들은 colon 을 세로로 맞춘다.

- 정렬 대상: 같은 부모 아래, 값이 한 줄짜리 scalar 인 형제 키들만(중첩 블록을 여는 헤더 키는 제외).
- 정렬 기준: 그 형제 그룹 안에서 가장 긴 키. 가장 긴 키는 `key:` 그대로, 나머지는 colon 위치가 맞도록 공백으로 채운 뒤 `: ` 한 칸 띄우고 값.
- 형제가 하나뿐이거나 전부 헤더 키면 정렬하지 않고 `key: value` 그대로.

예:
```yaml
spring:
  datasource:
    url     : jdbc:postgresql://localhost:54320/postgres
    username: postgres
    password: 1234
  ai:
    vectorstore:
      pgvector:
        initialize-schema: false
        dimensions       : 1024
        index-type       : HNSW
        distance-type    : COSINE_DISTANCE
        table-name       : vector_store
```

새로 만들거나 수정하는 블록에 적용. 안 건드리는 기존 블록까지 찾아가서 소급 정렬하지 않는다.
