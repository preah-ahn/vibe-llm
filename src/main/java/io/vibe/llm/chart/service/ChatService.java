package io.vibe.llm.chart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    public String getChatResponse(String query) {
        String systemMessage = new SystemPromptTemplate(new ClassPathResource("prompt/system-message.st")).render();
        String userMessage = new PromptTemplate(new ClassPathResource("prompt/user-message.st")).render(Map.of("concept", query));

        return chatClient.prompt()
                .system(systemMessage)
                .user(userMessage)
                .call()
                .content();
    }
}
