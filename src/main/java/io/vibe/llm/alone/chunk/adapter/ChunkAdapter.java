package io.vibe.llm.alone.chunk.adapter;

import com.knuddels.jtokkit.api.Encoding;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @since       2026.10.01
 * @author      preah
 * @description chunk adapter
 **********************************************************************************************************************/
@Component
@RequiredArgsConstructor
public class ChunkAdapter {

    private static final String HEADING_PATH_METADATA_KEY = "headingPath";

    /**
     * {@link MarkdownDocumentReader} 가 쓰는 commonmark 파서는 GFM 표를 모른다. 표는 {@link #splitTableSegments}
     * 로 미리 떼어내 reader 를 거치지 않으므로, 여기 남는 horizontalRuleCreateDocument/includeCodeBlock/includeBlockquote
     * 만 신경 쓰면 된다.
     * <p>
     * docling 이 변환한 마크다운은 원본 문서의 페이지/섹션 경계를 헤딩 없이 "---" 로만 표시할 때가 있다. 기본값(false)이면
     * 그 경계가 무시되어 서로 무관한 내용이 한 섹션(=한 임베딩 단위)으로 합쳐지므로, true 로 바꿔 "---" 에서도 섹션을 끊는다.
     */
    private static final MarkdownDocumentReaderConfig MARKDOWN_READER_CONFIG = MarkdownDocumentReaderConfig.builder()
            .withHorizontalRuleCreateDocument(true)
            .withIncludeCodeBlock(false)
            .withIncludeBlockquote(false)
            .build();

    /** 표 헤더/구분행("|---|---|")/본문행을 가르는 패턴. 구분행은 다단이어야만 매치되어, 수평선("---")과 구별된다. */
    private static final Pattern TABLE_DELIMITER_ROW = Pattern.compile("^\\s*\\|?\\s*:?-{1,}:?\\s*(\\|\\s*:?-{1,}:?\\s*)+\\|?\\s*$");

    /** ATX 헤딩("#"~"######" + 공백 + 제목) 패턴. 표를 바로 앞에서 끊어내 생기는, 본문 없는 헤딩을 직접 찾아내는 데 쓴다. */
    private static final Pattern ATX_HEADING = Pattern.compile("^(#{1,6})\\s+(.+?)\\s*$");

    private final TokenTextSplitter tokenTextSplitter;

    private final Encoding tokenEncoding;

    /**
     * 마크다운 텍스트를 표 블록과 나머지 텍스트로 먼저 나눈 뒤, 나머지 텍스트는 {@link MarkdownDocumentReader} 로
     * 섹션 단위로, 섹션이 과도하게 길면 {@link TokenTextSplitter} 로 한 번 더 쪼갠다. 표 블록은 행 중간이 끊기면
     * 의미가 깨지므로 쪼개지 않고 통째로 청크 하나로 유지한다.
     * <p>
     * 섹션/표 블록마다 상위 헤딩 경로를 metadata 로 붙여두면 {@link TokenTextSplitter} 가 재분할해도 그 경로가 하위
     * 청크에 그대로 복사되므로, 최종 청크 텍스트 맨 앞에 헤딩 경로를 붙여 어떤 섹션 소속인지 맥락을 유지한다.
     */
    public List<Chunk> toMarkdownChunk(String markdown) {
        Deque<Heading> headings = new ArrayDeque<>();
        List<Document> documents = new ArrayList<>();

        for (Segment segment : splitTableSegments(markdown)) {
            if (segment.table()) {
                documents.add(tableDocument(segment.content(), headings));
            }
            else {
                Resource resource = new ByteArrayResource(segment.content().getBytes(StandardCharsets.UTF_8));
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, MARKDOWN_READER_CONFIG);
                List<Document> sections = withHeadingPath(reader.get(), headings);
                documents.addAll(tokenTextSplitter.split(sections));
                applyTrailingHeadings(segment.content(), headings);
            }
        }

        return IntStream.range(0, documents.size())
                .mapToObj(index -> {
                    Document document = documents.get(index);
                    String text = withHeadingPrefix(document);
                    String headingPath = String.valueOf(document.getMetadata().getOrDefault(HEADING_PATH_METADATA_KEY, ""));
                    return new Chunk(index, text, text.length(), tokenEncoding.countTokensOrdinary(text), headingPath);
                })
                .toList();
    }

    /**
     * 마크다운을 줄 단위로 훑어 표 블록(헤더행 + 구분행 + 본문행)과 그 사이의 일반 텍스트를 원래 순서대로 나눈다.
     */
    private List<Segment> splitTableSegments(String markdown) {
        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        List<Segment> segments = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        int i = 0;
        while (i < lines.length) {
            boolean tableStart = isTableRow(lines[i]) && i + 1 < lines.length && TABLE_DELIMITER_ROW.matcher(lines[i + 1]).matches();
            if (tableStart) {
                if (!text.isEmpty()) {
                    segments.add(new Segment(false, text.toString()));
                    text.setLength(0);
                }
                int start = i;
                i += 2;
                while (i < lines.length && isTableRow(lines[i])) {
                    i++;
                }
                segments.add(new Segment(true, String.join("\n", Arrays.asList(lines).subList(start, i))));
            }
            else {
                text.append(lines[i]).append('\n');
                i++;
            }
        }
        if (!text.isEmpty()) {
            segments.add(new Segment(false, text.toString()));
        }

        return segments;
    }

    private boolean isTableRow(String line) {
        return StringUtils.hasText(line) && line.contains("|");
    }

    /**
     * 표로 바로 끊긴 텍스트 segment 는 "헤딩 바로 다음이 표라 본문이 없는" 헤딩을 포함할 수 있는데,
     * {@link MarkdownDocumentReader} 는 본문이 없는 헤딩은 Document 로 만들지 않고 그냥 버린다(category/title 유실).
     * segment 끝에서부터 거슬러 올라가며 연속된 헤딩 줄을 직접 찾아 스택에 반영해 이 유실을 보정한다.
     * (코드펜스 안의 "#" 로 시작하는 줄까지는 구분하지 않는다 — 표 바로 앞에 코드펜스가 오는 경우는 고려하지 않음.)
     */
    private void applyTrailingHeadings(String segmentContent, Deque<Heading> headings) {
        String[] lines = segmentContent.split("\n", -1);
        List<Heading> trailing = new ArrayList<>();

        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].strip();
            if (line.isEmpty()) {
                continue;
            }
            Matcher matcher = ATX_HEADING.matcher(line);
            if (!matcher.matches()) {
                break;
            }
            trailing.add(new Heading(matcher.group(1).length(), matcher.group(2)));
        }

        for (int i = trailing.size() - 1; i >= 0; i--) {
            Heading heading = trailing.get(i);
            while (!headings.isEmpty() && headings.peekLast().level() >= heading.level()) {
                headings.removeLast();
            }
            headings.addLast(heading);
        }
    }

    /** 표는 reader 를 거치지 않으므로 원본 마크다운(파이프/줄바꿈)을 그대로 텍스트로 쓰고, 현재 헤딩 경로만 metadata 로 붙인다. */
    private Document tableDocument(String tableMarkdown, Deque<Heading> headings) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "table");
        metadata.put(HEADING_PATH_METADATA_KEY, headings.stream().map(Heading::title).collect(Collectors.joining(" > ")));
        return Document.builder().text(tableMarkdown).metadata(metadata).build();
    }

    /**
     * 섹션을 문서 순서대로 훑으며 헤딩 레벨 스택을 유지해, 섹션마다 "상위 헤딩 > ... > 자기 헤딩" 경로를 metadata 로 붙인다.
     * {@link MarkdownDocumentReader} 는 섹션마다 직속 헤딩(category=header_N, title)만 알려주므로,
     * 조상 헤딩까지 이어지는 경로는 여기서 직접 조립해야 한다. 표 블록과 헤딩 경로를 공유하기 위해 스택을 외부에서 받는다.
     */
    private List<Document> withHeadingPath(List<Document> sections, Deque<Heading> headings) {
        List<Document> result = new ArrayList<>(sections.size());

        for (Document section : sections) {
            if (section.getMetadata().get("category") instanceof String category && category.startsWith("header_")) {
                int level = Integer.parseInt(category.substring("header_".length()));
                while (!headings.isEmpty() && headings.peekLast().level() >= level) {
                    headings.removeLast();
                }
                headings.addLast(new Heading(level, String.valueOf(section.getMetadata().get("title"))));
            }

            Map<String, Object> metadata = new HashMap<>(section.getMetadata());
            metadata.put(HEADING_PATH_METADATA_KEY, headings.stream().map(Heading::title).collect(Collectors.joining(" > ")));
            result.add(Document.builder().text(section.getText()).metadata(metadata).build());
        }

        return result;
    }

    private String withHeadingPrefix(Document document) {
        String text = document.getText();
        if (document.getMetadata().get(HEADING_PATH_METADATA_KEY) instanceof String headingPath && StringUtils.hasText(headingPath)) {
            return "[" + headingPath + "]\n" + text;
        }
        return text;
    }

    private record Heading(int level, String title) {
    }

    private record Segment(boolean table, String content) {
    }

    public record Chunk(int index, String text, int chars, int tokens, String headingPath) {
    }
}
