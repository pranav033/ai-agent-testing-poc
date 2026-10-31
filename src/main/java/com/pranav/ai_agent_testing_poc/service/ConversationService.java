
package com.pranav.ai_agent_testing_poc.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private final Map<ConversationKey, Long> lastReferencedOrderBySession =
            new ConcurrentHashMap<>();

    public void rememberOrder(
            UUID userId,
            String sessionId,
            Long orderId) {

        ConversationKey key = new ConversationKey(userId, sessionId);

        lastReferencedOrderBySession.put(key, orderId);
    }

    public Long getLastReferencedOrder(
            UUID userId,
            String sessionId) {

        ConversationKey key = new ConversationKey(userId, sessionId);

        return lastReferencedOrderBySession.get(key);
    }

    public void clearSession(
            UUID userId,
            String sessionId) {

        ConversationKey key = new ConversationKey(userId, sessionId);

        lastReferencedOrderBySession.remove(key);
    }
}
