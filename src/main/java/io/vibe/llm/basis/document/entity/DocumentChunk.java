package io.vibe.llm.basis.document.entity;

import io.vibe.llm.common.base.entity.Base;
import io.vibe.llm.common.engine.annotation.entity.Description;
import jakarta.persistence.*;
import lombok.*;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document chunk
 **********************************************************************************************************************/
@Entity
@Table(name = "tb_document_chunk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_chunk_id")
    @Description("문서청크아이디")
    private Long id;

    @Column(nullable = false)
    @Description("순서")
    private Integer chunkIndex;

    @Column(columnDefinition = "text", nullable = false)
    @Description("본문")
    private String text;

    @Column(nullable = false)
    @Description("글자수")
    private Integer chars;

    @Column(nullable = false)
    @Description("토큰수")
    private Integer tokens;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @Description("문서아이디")
    private Document document;
}
