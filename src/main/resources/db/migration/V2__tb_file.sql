-- 첨부 파일 메타데이터 테이블. common/attach 모듈이 저장한 파일의 경로/원본명/크기를 기록한다.

CREATE TABLE IF NOT EXISTS tb_file (
    file_id       bigint generated always as identity PRIMARY KEY,
    name          varchar(50)  NOT NULL,
    path          varchar(200) NOT NULL,
    original_name varchar(200) NOT NULL,
    size          bigint       NOT NULL,
    created_at    timestamp    NOT NULL
);

COMMENT ON TABLE  tb_file                IS '첨부파일';
COMMENT ON COLUMN tb_file.file_id        IS '파일아이디';
COMMENT ON COLUMN tb_file.name           IS '이름';
COMMENT ON COLUMN tb_file.path           IS '경로';
COMMENT ON COLUMN tb_file.original_name  IS '원본명';
COMMENT ON COLUMN tb_file.size           IS '크기';
COMMENT ON COLUMN tb_file.created_at     IS '등록일시';
