-- 임베딩 전 청킹 결과를 보관한다. 실제 임베딩된 벡터는 vector_store 에 별도로 들어간다.

CREATE TABLE IF NOT EXISTS tb_document_chunk (
    document_chunk_id bigint    generated always as identity PRIMARY KEY,
    document_id        bigint    NOT NULL REFERENCES tb_document (document_id),
    chunk_index         int       NOT NULL,
    text                text      NOT NULL,
    chars               int       NOT NULL,
    tokens              int       NOT NULL,
    created_at          timestamp NOT NULL,

    CONSTRAINT uk_tb_document_chunk_document_id_chunk_index UNIQUE (document_id, chunk_index)
);

COMMENT ON TABLE  tb_document_chunk                   IS '문서청크';
COMMENT ON COLUMN tb_document_chunk.document_chunk_id IS '문서청크아이디';
COMMENT ON COLUMN tb_document_chunk.document_id       IS '문서아이디';
COMMENT ON COLUMN tb_document_chunk.chunk_index       IS '순서';
COMMENT ON COLUMN tb_document_chunk.text              IS '본문';
COMMENT ON COLUMN tb_document_chunk.chars             IS '글자수';
COMMENT ON COLUMN tb_document_chunk.tokens            IS '토큰수';
COMMENT ON COLUMN tb_document_chunk.created_at        IS '등록일시';
