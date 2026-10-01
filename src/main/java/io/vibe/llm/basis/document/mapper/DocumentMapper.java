package io.vibe.llm.basis.document.mapper;

import io.vibe.llm.basis.document.entity.Document;
import io.vibe.llm.basis.document.form.DocumentForm.Request;
import io.vibe.llm.basis.document.form.DocumentForm.Response;
import io.vibe.llm.common.attach.entity.File;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document mapper
 **********************************************************************************************************************/
@Mapper(componentModel = "default", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

    Response.FindAll toFindAll(Document entity);
    Response.FindOne toFindOne(Document entity);
    Response.File toFile(File entity);

    Document toDocument(Request.Add form);
    Document toDocument(Long id, Request.Modify form);
    Document toDocument(Long id, Request.ChangeStatus form);
    File toFile(Request.File form);
}
