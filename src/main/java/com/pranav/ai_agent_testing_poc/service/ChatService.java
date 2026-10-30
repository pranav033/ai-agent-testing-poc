
package com.pranav.ai_agent_testing_poc.service;

import com.pranav.ai_agent_testing_poc.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\\border\\s*(?:#|number\\s*)?(\\d+)\\b");

    private final ConversationService conversationService;

    public ChatService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    public ChatResponse processMessage(String sessionId, String message) {

        if (sessionId == null || sessionId.isBlank()) {
            return new ChatResponse(
                    sessionId,
                    "Session ID is required.",
                    false,
                    false
            );
        }

        if (message == null || message.isBlank()) {
            return new ChatResponse(
                    sessionId,
                    "Please provide a message.",
                    false,
                    false
            );
        }

        String normalizedMessage = message
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[?.!]+$", "");

        // -----------------------------
        // 1. Guardrail scenarios
        // -----------------------------
        if (isUnsafeRequest(normalizedMessage)) {
            return new ChatResponse(
                    sessionId,
                    "I can't help with that request.",
                    false,
                    true
            );
        }

        // -----------------------------
        // 2. Explicit order reference
        // -----------------------------
        Long referencedOrderId = extractOrderId(normalizedMessage);

        if (referencedOrderId != null) {

            conversationService.rememberOrder(sessionId, referencedOrderId);

            return getOrderResponse(sessionId, referencedOrderId);
        }

        // -----------------------------
        // 3. Context-dependent request
        // -----------------------------
        if (isContextDependentOrderRequest(normalizedMessage)) {

            Long previousOrderId =
                    conversationService.getLastReferencedOrder(sessionId);

            if (previousOrderId == null) {
                return new ChatResponse(
                        sessionId,
                        "Which order are you referring to? Please provide the order number.",
                        false,
                        false
                );
            }

            return getOrderResponse(sessionId, previousOrderId);
        }

        // -----------------------------
        // 4. Ambiguous request
        // -----------------------------
        if (isAmbiguousOrderRequest(normalizedMessage)) {
            return new ChatResponse(
                    sessionId,
                    "I can help with an order, but I need the order number first.",
                    false,
                    false
            );
        }

        // -----------------------------
        // 5. Unknown / unsupported query
        // -----------------------------
        return new ChatResponse(
                sessionId,
                "I don't have enough information to answer that accurately.",
                false,
                false
        );
    }

    private Long extractOrderId(String message) {

        Matcher matcher = ORDER_ID_PATTERN.matcher(message);

        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        return null;
    }

    private ChatResponse getOrderResponse(
            String sessionId,
            Long orderId) {

        return switch (orderId.intValue()) {

            case 101 -> new ChatResponse(
                    sessionId,
                    "Order 101 is currently shipped.",
                    true,
                    false
            );

            case 102 -> new ChatResponse(
                    sessionId,
                    "Order 102 is currently out for delivery.",
                    true,
                    false
            );

            default -> new ChatResponse(
                    sessionId,
                    "I couldn't find reliable information for order "
                            + orderId + ".",
                    false,
                    false
            );
        };
    }

    private boolean isContextDependentOrderRequest(String message) {

        return message.contains("where is it")
                || message.contains("what is its status")
                || message.contains("where is my order now")
                || message.equals("status")
                || message.equals("where is it now");
    }

    private boolean isAmbiguousOrderRequest(String message) {

        return message.equals("where is my order")
                || message.equals("check my order")
                || message.equals("order status");
    }

    // -----------------------------
    // Security guardrail detection
    // -----------------------------
    private boolean isUnsafeRequest(String message) {

        // Attempts to override the chatbot's instructions
        boolean attemptsInstructionOverride = containsAny(
                message,
                "ignore previous instructions",
                "ignore all previous instructions",
                "ignore your instructions",
                "disregard previous instructions",
                "forget all previous instructions",
                "developer mode"
        );

        // Attempts to access hidden or sensitive information
        boolean requestsSensitiveInformation = containsAny(
                message,
                "system prompt",
                "hidden prompt",
                "internal instructions",
                "hidden configuration",
                "developer instructions",
                "secret configuration"
        );

        // Attempts to disclose sensitive information
        boolean requestsDisclosure = containsAny(
                message,
                "reveal",
                "print",
                "show",
                "display",
                "dump",
                "expose",
                "output"
        );

        // Potentially harmful requests
        boolean harmfulRequest = containsAny(
                message,
                "steal",
                "hack",
                "bypass security",
                "disable security"
        );

        return attemptsInstructionOverride
                || harmfulRequest
                || (requestsSensitiveInformation && requestsDisclosure);
    }

    // Checks whether the message contains any specified phrase
    private boolean containsAny(String message, String... phrases) {

        for (String phrase : phrases) {
            if (message.contains(phrase)) {
                return true;
            }
        }

        return false;
    }
}
