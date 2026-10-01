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
