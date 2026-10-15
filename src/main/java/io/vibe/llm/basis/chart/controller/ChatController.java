package io.vibe.llm.basis.chart.controller;

import io.vibe.llm.basis.chart.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @since       2026.09.21
 * @author      preah
 * @description chat controller
 **********************************************************************************************************************/
@RestController
@RequiredArgsConstructor
@RequestMapping("${property.api.end-point}")
public class ChatController {

    private final ChatService chatService;

//    @GetMapping("/chats")
//    public ResponseEntity<Tutorial> getChart(@RequestParam(value="query", defaultValue="안녕") String query) {
//        Tutorial response = chatService.getChatResponse(query);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/chats")
    public ResponseEntity<ChatAnswer> getChart(@RequestParam(value="query") String query, @RequestHeader("userId") String userId) {
        ChatAnswer response = chatService.getChatResponse(query, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat-streams")
    public ResponseEntity<Flux<String>> getChartStream(@RequestParam(value="query") String query,
                                                       @RequestParam(value="conversationId") String conversationId) {
        Flux<String> response = chatService.getChatStream(query, conversationId);
        return ResponseEntity.ok(response);
    }
}
