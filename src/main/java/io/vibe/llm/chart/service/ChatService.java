package io.vibe.llm.chart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    public String getChatResponse(String query) {
        Prompt prompt = new Prompt(query);
        return chatClient.prompt(prompt).call().content();
    }
}
