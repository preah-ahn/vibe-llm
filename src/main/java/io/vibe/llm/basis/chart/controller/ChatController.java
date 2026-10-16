package io.vibe.llm.basis.chart.controller;

import io.vibe.llm.basis.chart.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/chats")
    public ResponseEntity<Flux<String>> getChartStream(@RequestParam(value="query") String query, @RequestParam(value="conversationId") String conversationId) {
        Flux<String> response = chatService.getChatStream(query, conversationId);
        return ResponseEntity.ok(response);
    }
}
