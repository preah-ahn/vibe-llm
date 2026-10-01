package io.vibe.llm.alone.docling.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/**
 * @since       2026.10.01
 * @author      preah
 * @description docling adapter
 **********************************************************************************************************************/
@Component
public class DoclingAdapter {

    private final RestClient restClient;

    public DoclingAdapter(RestClient doclingRestClient) {
        this.restClient = doclingRestClient;
    }

    /** 문서 전체를 마크다운으로 변환한다. */
    public String toMarkdown(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file.getResource());
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
