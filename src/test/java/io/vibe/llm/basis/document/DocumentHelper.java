package io.vibe.llm.basis.document;

import io.vibe.llm.basis.document.enumerate.DocumentStatusType;
import io.vibe.llm.basis.document.form.DocumentForm.Request;
import io.vibe.llm.basis.document.form.DocumentForm.Response;
import lombok.SneakyThrows;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StringUtils;

import static io.vibe.llm.common.engine.helper.model.ObjectHelper.toInstance;
import static io.vibe.llm.common.engine.helper.model.ObjectHelper.toJson;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document helper
 **********************************************************************************************************************/
public class DocumentHelper {

    static MockMvc mock;

    @SneakyThrows
    public static void getPage(Request.Find find) {
        var request = MockMvcRequestBuilders.get("/api/documents/pages");
        if (StringUtils.hasText(find.getName())) {
            request.param("name", find.getName());
        }

        mock.perform(request)
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    public static void get(Long documentId) {
        mock.perform(MockMvcRequestBuilders.get("/api/documents/{documentId}", documentId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @SneakyThrows
    public static Long add(Request.Add add) {
        return toFindOne(mock.perform(post("/api/documents")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(add)))
                .andExpect(status().isOk())
                .andDo(print())).getId();
    }

    @SneakyThrows
    public static Long modify(Long documentId, Request.Modify modify) {
        return toFindOne(mock.perform(put("/api/documents/{documentId}", documentId)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(modify)))
                .andExpect(status().isOk())
                .andDo(print())).getId();
    }

    @SneakyThrows
    public static Long changeStatus(Long documentId, Request.ChangeStatus changeStatus) {
        return toFindOne(mock.perform(patch("/api/documents/{documentId}/status", documentId)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(changeStatus)))
                .andExpect(status().isOk())
                .andDo(print())).getId();
    }

    @SneakyThrows
    public static void remove(Long documentId) {
        mock.perform(delete("/api/documents/{documentId}", documentId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    public static Request.Find findDocument() {
        return Request.Find.builder().build();
    }

    public static Request.Add addDocument() {
        return Request.Add.builder()
                .name("테스트 문서")
                .description("설명")
                .url("https://example.com")
                .file(documentFile())
                .build();
    }

    public static Request.Modify modifyDocument() {
        return Request.Modify.builder()
                .name("수정된 문서")
                .description("수정된 설명")
                .url("https://example.com/modified")
                .file(documentFile())
                .build();
    }

    public static Request.File documentFile() {
        return Request.File.builder()
                .path("2026/10/1")
                .name("테스트파일.pdf")
                .originalName("원본파일.pdf")
                .size(1024L)
                .build();
    }

    public static Request.ChangeStatus changeStatusDocument() {
        return Request.ChangeStatus.builder()
                .statusType(DocumentStatusType.CHUNKING_IN_COMPLETED)
                .build();
    }

    /** ObjectHelper.toInstance 는 ResultActions 오버로드가 없다(MockMvc 는 테스트 소스에만 있어 메인 소스에 못 둔다). */
    @SneakyThrows
    private static Response.FindOne toFindOne(ResultActions resultActions) {
        return toInstance(Response.FindOne.class, resultActions.andReturn().getResponse().getContentAsString());
    }
}
