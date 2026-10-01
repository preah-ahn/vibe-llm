package io.vibe.llm.basis.document.entity;

import io.vibe.llm.basis.document.enumerate.DocumentStatusType;
import io.vibe.llm.common.attach.entity.File;
import io.vibe.llm.common.base.entity.Base;
import io.vibe.llm.common.engine.annotation.entity.Description;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document
 **********************************************************************************************************************/
@Entity
@Table(name = "tb_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    @Description("문서아이디")
    private Long id;

    @Column(length = 200, nullable = false)
    @Description("이름")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    @Description("상태구분")
    private DocumentStatusType statusType;

    @Column(length = 4000)
    @Description("설명")
    private String description;

    @Column(length = 4000)
    @Description("URL")
    private String url;

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Description("파일")
    private File file;

    @Column(columnDefinition = "text")
    @Description("마크다운")
    private String markdown;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Description("청크-목록")
    private List<DocumentChunk> chunks = new ArrayList<>();
}
