# 로컬 환경 구성

## 사전 준비

| 항목 | 요구사항 |
|---|---|
| JDK | 21 (Gradle toolchain 이 자동 선택) |
| Docker | pgvector + docling-serve 컨테이너 실행용 |
| LM Studio | `127.0.0.1:12345` 에서 OpenAI 호환 서버 실행 |

LM Studio 에는 **두 모델을 동시에 로드**해야 한다.

- chat: `qwen3.5-4b-super-coder`
- embedding: `text-embedding-qwen3-embedding-0.6b`

VRAM 이 부족해 한쪽이 언로드되면 적재 시 `/v1/embeddings` 404, 대화 중
`/v1/chat/completions` 404 로 나타난다. 이때는 적재를 먼저 끝내고 대화하거나
LM Studio 의 멀티 모델 로딩을 켠다.

## 1. 인프라 기동 (pgvector + docling-serve)

```sh
docker compose -f src/main/resources/docker/docker-compose.yml up -d
docker compose -f src/main/resources/docker/docker-compose.yml ps
```

| 서비스 | 이미지 | 호스트 포트 | 용도 |
|---|---|---|---|
| `pg-vector` | `pgvector/pgvector:pg17` | 54320 | 벡터 스토어 + 대화 메모리 |
| `docling-serve` | `quay.io/docling-project/docling-serve-cpu:v1.35.0` | 50010 | 문서 파싱 + 청킹 |

두 포트 모두 `application.yaml` 과 반드시 일치해야 한다 — 54320 은
`spring.datasource.url`, 50010 은 `docling.base-url`.

`src/main/resources/docker/docker.sh` 는 compose 이전에 쓰던 pgvector 단독 기동
스크립트다. docling 이 없으므로 문서 적재가 동작하지 않는다.

확인:

```sh
docker exec vibe-llm-pg-vector-1 psql -U postgres -d postgres -c "select 1"
docker exec vibe-llm-pg-vector-1 psql -U postgres -d postgres -c "\dx"
curl http://localhost:50010/health    # {"status":"ok"}
```

`vector`, `hstore`, `uuid-ossp` 확장이 **사용 가능**해야 한다. 실제 설치는 앱이
`spring.ai.vectorstore.pgvector.initialize-schema: true` 로 기동할 때 수행된다.

docling-serve 는 CPU 이미지라 **최초 기동 시 모델 적재에 몇 분** 걸린다. compose 의
healthcheck `start_period` 를 180s 로 둔 이유다. `/health` 가 응답해야 적재가 가능하다.
`http://localhost:50010/ui` 에서 변환 결과를 직접 확인할 수도 있다.

## 2. 임베딩 차원 실측

`spring.ai.vectorstore.pgvector.dimensions` 는 임베딩 모델의 실제 출력 길이와
**정확히 같아야 한다.** 추측하지 말고 확인한다.

```sh
curl -s http://127.0.0.1:12345/v1/models
curl -s http://127.0.0.1:12345/v1/embeddings \
  -H "Content-Type: application/json" \
  -d '{"model":"text-embedding-qwen3-embedding-0.6b","input":"테스트"}'
```

`data[0].embedding` 의 길이가 차원 수다. 현재 모델은 **1024**.

차원을 바꾸면 기존 테이블과 충돌하므로 `DROP TABLE vector_store;` 후 재기동한다.
불일치는 기동 시점이 아니라 **첫 적재 시점**에 `expected N dimensions` 오류로 드러난다.

## 3. 실행

```sh
./gradlew bootRun
```

기동 로그에서 확인할 것:

```
PgVectorStore : Initializing PGVectorStore schema for table: vector_store in schema: public
Started Application in ... seconds
```

스키마 확인:

```sh
docker exec vibe-llm-pg-vector-1 psql -U postgres -d postgres -c "\d vector_store"
```

기대값 — `embedding vector(1024)` 와 `spring_ai_vector_index` HNSW 인덱스.

## 4. 동작 검증

### 문서 적재

```sh
curl -F "file=@sample.pdf" http://localhost:8081/documents
# {"source":"sample.pdf","chunks":12}
```

docling-serve 가 응답하지 않으면 5xx 가 난다. 앱에 대체 파서가 없으므로 컨테이너
상태를 먼저 확인한다.

```sh
curl http://localhost:50010/health    # {"status":"ok"}
```

저장 없이 파싱·청킹 결과만 보려면:

```sh
curl -F "file=@sample.pdf" http://localhost:8081/documents/analysis
# markdown 전문 + 청크별 text/tokens
```

저장 확인:

```sql
SELECT metadata->>'source' AS source, count(*) FROM vector_store GROUP BY 1;
SELECT vector_dims(embedding) FROM vector_store LIMIT 1;
```

같은 파일을 다시 올려도 총 건수가 늘지 않아야 한다(교체 동작).

### RAG 대화

```sh
curl "http://localhost:8081/rag-chats?query=<문서에만 있는 내용>"
```

같은 질문을 기존 엔드포인트에 던져 비교한다.

```sh
curl -H "userId: u1" "http://localhost:8081/chats?query=<같은 질문>"
```

문서 내용이 RAG 쪽에만 반영되면 파이프라인이 정상이다.

`/ui` 비교 화면이 쓰는 스트리밍 변형도 같은 방식으로 확인한다. `-N` 을 빼면 curl 이
버퍼링해서 스트리밍 여부를 볼 수 없다.

```sh
curl -N "http://localhost:8081/rag-chat-streams?query=<같은 질문>"
curl -N -H "userId: u1" "http://localhost:8081/plain-chat-streams?query=<같은 질문>"
```

### 화면

`http://localhost:8081/` 로 접속하면 `/ui` 로 이동한다. 세 화면이 있다.

| URL | 화면 |
|---|---|
| `/ui` | RAG 비교 채팅 — 같은 질문을 RAG 유무로 나란히 비교 (양쪽 스트리밍) |
| `/ui/documents` | 문서 적재 + 목록/삭제 |
| `/ui/stream` | 스트리밍 채팅 |

화면은 브라우저가 한글을 자동으로 인코딩하므로 아래 인코딩 문제를 겪지 않는다.
상세 설계는 [design.md](design.md) 참고.

### Swagger

`http://localhost:8081/swagger-ui.html` 에서 파일 업로드를 바로 테스트할 수 있다.

## 한글 쿼리 인코딩 주의

터미널 로케일이 UTF-8 이 아니면 한글 쿼리 파라미터가 깨져 Tomcat 이 **400** 을 반환한다.

```
InvalidParameterException: Character decoding failed. Parameter [query] ... has been ignored.
```

앱 문제가 아니라 클라이언트 인코딩 문제다. 미리 퍼센트 인코딩해서 보내면 된다.

```sh
python -c "import urllib.parse;print(urllib.parse.quote('한글 질문',safe=''))"
```

Swagger UI 나 IntelliJ HTTP Client 를 쓰면 이 문제가 없다.

## 자격 증명 참고

`application.yaml` 의 `spring.datasource.password`(`1234`)와
`spring.ai.openai.api-key`(`lm-studio`)는 로컬 전용 placeholder 로, 현재 저장소에 그대로 들어 있다.
실제 자격 증명은 이 파일에 두지 말고 환경 변수나 외부 설정으로 분리한다.
