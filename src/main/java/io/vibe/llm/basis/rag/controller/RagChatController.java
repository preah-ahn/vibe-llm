package io.vibe.llm.basis.rag.controller;

import io.vibe.llm.basis.rag.service.RagChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @since       2026.10.01
 * @author      preah
 * @description rag chat controller
 **********************************************************************************************************************/
@RestController
@RequiredArgsConstructor
public class RagChatController {

    private final RagChatService ragChatService;

    @GetMapping("/rag-chats")
    public ResponseEntity<String> ragChat(@RequestParam(value="query") String query,
                                         @RequestParam(value="conversationId", defaultValue="default") String conversationId) {
        String response = ragChatService.ask(query, conversationId);
        return ResponseEntity.ok(response);
    }

    /** {@code /rag-chats} 와 같은 응답을 토큰 단위로 흘려보낸다. 비교 화면이 쓴다. */
    @GetMapping("/rag-chat-streams")
    public ResponseEntity<Flux<String>> ragChatStream(@RequestParam(value="query") String query,
                                                      @RequestParam(value="conversationId", defaultValue="default") String conversationId) {
        Flux<String> response = ragChatService.askStream(query, conversationId);
        return ResponseEntity.ok(response);
    }
}
