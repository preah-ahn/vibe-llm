-- Document ↔ File 을 1:N 에서 1:1 로 변경. tb_file.document_id 에 유니크 제약을 걸어 문서당 파일 하나로 제한한다.

ALTER TABLE tb_file
    ADD CONSTRAINT uk_tb_file_document_id UNIQUE (document_id);
