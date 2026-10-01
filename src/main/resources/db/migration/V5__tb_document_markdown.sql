-- docling 변환 결과(문서 전문)를 저장한다. 길이 제한이 없어야 해서 text 로 둔다.

ALTER TABLE tb_document
    ADD COLUMN IF NOT EXISTS markdown text;

COMMENT ON COLUMN tb_document.markdown IS '마크다운';
