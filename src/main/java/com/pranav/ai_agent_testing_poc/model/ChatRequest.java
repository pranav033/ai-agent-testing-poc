package com.pranav.ai_agent_testing_poc.model;

public record ChatRequest(
        String sessionId,
        String message
) {
}