
package com.pranav.ai_agent_testing_poc.service;

import java.util.Objects;
import java.util.UUID;

public record ConversationKey(
        UUID userId,
        String sessionId
) {

    public ConversationKey {
        Objects.requireNonNull(userId, "User ID is required.");

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Session ID is required."
            );
        }
    }
}
