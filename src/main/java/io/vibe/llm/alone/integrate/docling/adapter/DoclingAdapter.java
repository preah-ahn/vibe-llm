package io.vibe.llm.alone.integrate.docling.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vibe.llm.common.attach.entity.File;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @since       2026.10.01
 * @author      preah
 * @description docling adapter
 **********************************************************************************************************************/
@Component
@RequiredArgsConstructor
public class DoclingAdapter {

    private final RestClient restClient;

    @Value("${property.attach.store-location}")
    private String storeLocation;

    /** 업로드 중인 파일을 마크다운으로 변환한다. 청킹은 Spring AI 의 {@code TokenTextSplitter} 가 담당한다. */
    public String toMarkdown(MultipartFile file) {
        return convert(file.getResource());
    }

    /** attach 로 이미 저장된 파일을 마크다운으로 변환한다. */
    public String toMarkdown(File file) {
        Path path = Paths.get(storeLocation, "document").resolve(file.getPath()).resolve(file.getName());
        return convert(new FileSystemResource(path));
    }

    private String convert(Resource resource) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", resource);
        body.add("to_formats", "md");

        ConvertResponse response = restClient.post()
                .uri("/v1/convert/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(ConvertResponse.class);

        String markdown = response == null || response.document() == null
                ? null
                : response.document().mdContent();
        return markdown == null ? "" : markdown;
    }

    private record ConvertResponse(ExportDocument document) {
    }

    private record ExportDocument(@JsonProperty("md_content") String mdContent) {
    }
}
