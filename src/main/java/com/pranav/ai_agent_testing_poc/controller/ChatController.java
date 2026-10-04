package com.pranav.ai_agent_testing_poc.controller;

import com.pranav.ai_agent_testing_poc.model.ChatRequest;
import com.pranav.ai_agent_testing_poc.model.ChatResponse;
import com.pranav.ai_agent_testing_poc.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        return chatService.processMessage(
                request.sessionId(),
                request.message()
        );
    }
}