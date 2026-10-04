package com.pranav.ai_agent_testing_poc.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private final Map<String, Long> lastReferencedOrderBySession =
            new ConcurrentHashMap<>();

    public void rememberOrder(String sessionId, Long orderId) {
        lastReferencedOrderBySession.put(sessionId, orderId);
    }

    public Long getLastReferencedOrder(String sessionId) {
        return lastReferencedOrderBySession.get(sessionId);
    }

    public void clearSession(String sessionId) {
        lastReferencedOrderBySession.remove(sessionId);
    }
}