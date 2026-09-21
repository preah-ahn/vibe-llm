package io.vibe.llm.chart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;

    public String getChatResponse(String query) {
        String response = chatModel.call(query);
        return response;
    }
}
