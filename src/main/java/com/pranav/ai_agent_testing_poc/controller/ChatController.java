
package com.pranav.ai_agent_testing_poc.controller;

import com.pranav.ai_agent_testing_poc.model.ChatRequest;
import com.pranav.ai_agent_testing_poc.model.ChatResponse;
import com.pranav.ai_agent_testing_poc.security.AppUserDetails;
import com.pranav.ai_agent_testing_poc.service.ChatService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(
            @AuthenticationPrincipal AppUserDetails authenticatedUser,
            @RequestBody ChatRequest request) {

        if (authenticatedUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required."
            );
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chat request is required."
            );
        }

        return chatService.processMessage(
                authenticatedUser.getUserId(),
                authenticatedUser.isAdmin(),
                request.sessionId(),
                request.message()
        );
    }
}
