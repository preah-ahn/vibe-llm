package io.vibe.llm.chart.controller;

import io.vibe.llm.chart.entity.Tutorial;
import io.vibe.llm.chart.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

//    @GetMapping("/chats")
//    public ResponseEntity<Tutorial> getChart(@RequestParam(value="query", defaultValue="안녕") String query) {
//        Tutorial response = chatService.getChatResponse(query);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/chats")
    public ResponseEntity<String> getChart(@RequestParam(value="query") String query, @RequestHeader("userId") String userId) {
        String response = chatService.getChatResponse(query, userId);
        return ResponseEntity.ok(response);
    }

    /** {@code /chats} 와 같은 응답을 토큰 단위로 흘려보낸다. RAG 비교 화면이 쓴다. */
    @GetMapping("/plain-chat-streams")
    public ResponseEntity<Flux<String>> getPlainChartStream(@RequestParam(value="query") String query,
                                                            @RequestHeader("userId") String userId) {
        Flux<String> response = chatService.getPlainStreamChat(query, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat-streams")
    public ResponseEntity<Flux<String>> getChartStream(@RequestParam(value="query", defaultValue="안녕") String query,
                                                       @RequestParam(value="conversationId", defaultValue="default") String conversationId) {
        Flux<String> response = chatService.getStreamChat(query, conversationId);
        return ResponseEntity.ok(response);
    }
}
