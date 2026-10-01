-- Spring AI 가 initialize-schema 로 만들던 테이블을 Flyway 가 소유하도록 이관한다.
-- 운영 중인 개발 DB 에 이미 존재할 수 있어 전부 IF NOT EXISTS 로 작성했다.

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 차원 1024 는 application.yaml 의 spring.ai.vectorstore.pgvector.dimensions 와 일치해야 한다.
-- 변경하려면 적재 데이터를 모두 재임베딩해야 하므로 이 파일을 고치지 말고 새 마이그레이션을 추가한다.
CREATE TABLE IF NOT EXISTS vector_store (
    id        uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    content   text,
    metadata  json,
    embedding vector(1024)
);

CREATE INDEX IF NOT EXISTS spring_ai_vector_index
    ON vector_store USING hnsw (embedding vector_cosine_ops);

CREATE TABLE IF NOT EXISTS spring_ai_chat_memory (
    conversation_id varchar(36) NOT NULL,
    content         text        NOT NULL,
    type            varchar(10) NOT NULL,
    "timestamp"     timestamp   NOT NULL,
    sequence_id     bigint      NOT NULL,
    CONSTRAINT spring_ai_chat_memory_type_check
        CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL'))
);

CREATE INDEX IF NOT EXISTS spring_ai_chat_memory_conversation_id_timestamp_idx
    ON spring_ai_chat_memory (conversation_id, "timestamp");

CREATE INDEX IF NOT EXISTS spring_ai_chat_memory_conversation_id_sequence_id_idx
    ON spring_ai_chat_memory (conversation_id, sequence_id);
