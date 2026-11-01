
package com.pranav.ai_agent_testing_poc.service;

public class OrderAccessDeniedException extends RuntimeException {

    public OrderAccessDeniedException(String message) {
        super(message);
    }
}
