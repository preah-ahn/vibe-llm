package io.vibe.llm.common.attach.controller;

import io.vibe.llm.common.attach.service.AttachService;
import io.vibe.llm.common.attach.form.AttachForm;
import io.vibe.llm.common.attach.mapper.AttachMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @since       2026.10.01
 * @author      preah
 * @description attach controller
 **********************************************************************************************************************/
@RestController
@RequiredArgsConstructor
public class AttachController {

    private final AttachService attachService;

    @PostMapping(value = "/attaches", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachForm.Response.FindOne add(@RequestPart("file") MultipartFile file) {
        return AttachMapper.mapper.toFindOne(attachService.add(file));
    }

    @DeleteMapping("/attaches")
    public void remove(@Valid AttachForm.Request.Remove remove) {
        attachService.remove(AttachMapper.mapper.toAttach(remove));
    }
}
