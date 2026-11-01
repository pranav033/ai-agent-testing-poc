
package com.pranav.ai_agent_testing_poc.controller;

import com.pranav.ai_agent_testing_poc.service.OrderAccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleOrderAccessDenied(
            OrderAccessDeniedException exception) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "FORBIDDEN",
                        "message", exception.getMessage()
                ));
    }
}
