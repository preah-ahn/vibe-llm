package io.vibe.llm.common.attach.entity;

import io.vibe.llm.basis.document.entity.Document;
import io.vibe.llm.common.base.entity.Base;
import io.vibe.llm.common.engine.annotation.entity.Description;
import jakarta.persistence.*;
import lombok.*;

/**
 * @since       2026.10.01
 * @author      preah
 * @description file
 **********************************************************************************************************************/
@Entity
@Table(name = "tb_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    @Description("파일아이디")
    private Long id;

    @Column(length = 50, nullable = false)
    @Description("이름")
    private String name;

    @Column(length = 200, nullable = false)
    @Description("경로")
    private String path;

    @Column(length = 200, nullable = false)
    @Description("원본명")
    private String originalName;

    @Column(nullable = false)
    @Description("크기")
    private Long size;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", unique = true)
    @Description("문서아이디")
    private Document document;
}
