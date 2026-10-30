
package com.pranav.ai_agent_testing_poc.model;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
