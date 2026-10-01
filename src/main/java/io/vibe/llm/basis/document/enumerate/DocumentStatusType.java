package io.vibe.llm.basis.document.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document status type
 **********************************************************************************************************************/
@Getter
@AllArgsConstructor
public enum DocumentStatusType {

     WAITING               ("대기")
    ,MARKDOWN_IN_COMPLETED ("마크다운-완료")
    ,CHUNKING_IN_COMPLETED ("청킹-완료")
    ,COMPLETED             ("완료");
    private String description;
}