package io.vibe.llm.rag.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

/**
 * docling-serve 클라이언트. 파싱과 청킹을 모두 docling 에 위임한다.
 * <p>
 * 입력 형식 판별은 docling 이 파일명 확장자로 수행하므로 파트의 Content-Type 은 지정하지 않는다.
 * 청킹 옵션은 전부 docling 기본값(HybridChunker, all-MiniLM-L6-v2 토크나이저, merge_peers)을 쓴다.
 */
@Component
public class DoclingClient {

    // 청크 토큰 상한. 지정하지 않으면 docling 이 250 을 쓴다(실측).
    // 기준 토크나이저는 chunking_tokenizer 기본값인 all-MiniLM-L6-v2 이므로,
    // 임베딩 모델(Qwen3)의 실제 토큰 수와는 다르다.
    private static final int CHUNK_MAX_TOKENS = 1000;

    private final RestClient restClient;

    public DoclingClient(@Value("${docling.base-url}") String baseUrl,
                         @Value("${docling.read-timeout}") Duration readTimeout) {
        // CPU 이미지는 문서 한 건 변환에 수십 초가 걸린다. 기본 read timeout 으로는 끊긴다.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /** 문서 전체를 마크다운으로 변환한다. 화면 표시용이며 청킹에는 쓰이지 않는다. */
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

    /**
     * HybridChunker 로 청킹한다. 청크 텍스트는 상위 제목이 앞에 붙은 contextualized 텍스트라
     * 그대로 임베딩하면 검색 품질에 유리하다.
     * <p>
     * {@code max_tokens} 외의 옵션은 docling 기본값을 쓴다. overlap 은 docling-serve 가
     * 지원하지 않는다 — v1.35.0 의 어느 청커 옵션에도 없다.
     */
    public List<Chunk> chunk(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file.getResource());
        body.add("chunking_max_tokens", CHUNK_MAX_TOKENS);

        ChunkResponse response = restClient.post()
                .uri("/v1/chunk/hybrid/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(ChunkResponse.class);

        return response == null || response.chunks() == null ? List.of() : response.chunks();
    }

    /** @param numTokens docling 이 청킹에 쓴 토크나이저 기준 토큰 수 */
    public record Chunk(
            @JsonProperty("chunk_index") int index,
            String text,
            @JsonProperty("num_tokens") Integer numTokens) {
    }

    private record ChunkResponse(List<Chunk> chunks) {
    }

    private record ConvertResponse(ExportDocument document) {
    }

    private record ExportDocument(@JsonProperty("md_content") String mdContent) {
    }
}
