package com.pranav.ai_agent_testing_poc.model;

public record ChatResponse(
        String sessionId,
        String response,
        boolean taskCompleted,
        boolean blocked
) {
}