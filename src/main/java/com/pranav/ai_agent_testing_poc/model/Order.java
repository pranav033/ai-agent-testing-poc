
package com.pranav.ai_agent_testing_poc.model;

import java.util.UUID;

public record Order(
        Long orderId,
        UUID ownerUserId,
        String status
) {
}
