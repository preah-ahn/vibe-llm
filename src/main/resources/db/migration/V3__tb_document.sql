-- 문서 테이블. Document 엔티티와 매핑되며, tb_file 과 1:N 관계(tb_file.document_id)를 맺는다.

CREATE TABLE IF NOT EXISTS tb_document (
    document_id bigint generated always as identity PRIMARY KEY,
    name        varchar(200)   NOT NULL,
    status_type varchar(50)    NOT NULL,
    description varchar(4000),
    url         varchar(4000),
    markdown    text,
    created_at  timestamp      NOT NULL
);

COMMENT ON TABLE  tb_document             IS '문서';
COMMENT ON COLUMN tb_document.document_id IS '문서아이디';
COMMENT ON COLUMN tb_document.name        IS '이름';
COMMENT ON COLUMN tb_document.status_type IS '상태구분';
COMMENT ON COLUMN tb_document.description IS '설명';
COMMENT ON COLUMN tb_document.url         IS 'URL';
COMMENT ON COLUMN tb_document.markdown    IS '마크다운';
COMMENT ON COLUMN tb_document.created_at  IS '등록일시';

-- File.document(@ManyToOne) 쪽 FK. 문서와 무관한 첨부도 있어 NULL 허용한다.
ALTER TABLE tb_file
    ADD COLUMN IF NOT EXISTS document_id bigint REFERENCES tb_document (document_id);

COMMENT ON COLUMN tb_file.document_id IS '문서아이디';
