package io.vibe.llm.chart.controller;

import io.vibe.llm.chart.entity.Tutorial;
import io.vibe.llm.chart.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> getChart(@RequestParam(value="query", defaultValue="안녕") String query) {
        String response = chatService.getChatResponse(query);
        return ResponseEntity.ok(response);
    }
}
