package io.vibe.llm.common.attach.mapper;

import io.vibe.llm.common.attach.domain.Attach;
import io.vibe.llm.common.attach.form.AttachForm;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @since       2026.10.01
 * @author      preah
 * @description attach mapper
 **********************************************************************************************************************/
@Mapper(componentModel = "default", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttachMapper {

    AttachMapper mapper = Mappers.getMapper(AttachMapper.class);

    Attach toAttach (AttachForm.Request.Remove form);
    AttachForm.Response.FindOne toFindOne(Attach entity);
}
