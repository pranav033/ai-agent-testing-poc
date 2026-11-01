
package com.pranav.ai_agent_testing_poc.service;

import com.pranav.ai_agent_testing_poc.model.Order;
import com.pranav.ai_agent_testing_poc.security.DemoUserIds;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final Map<Long, Order> orders;

    public OrderService() {

        Order aliceOrder = new Order(
                101L,
                DemoUserIds.ALICE,
                "shipped"
        );

        Order bobOrder = new Order(
                102L,
                DemoUserIds.BOB,
                "out for delivery"
        );

        this.orders = Map.of(
                101L, aliceOrder,
                102L, bobOrder
        );
    }

    public Optional<Order> findAccessibleOrder(
            UUID authenticatedUserId,
            boolean isAdmin,
            Long orderId) {

        if (authenticatedUserId == null) {
            throw new IllegalArgumentException(
                    "Authenticated user ID is required."
            );
        }

        if (orderId == null) {
            return Optional.empty();
        }

        // Step 1: Retrieve the requested order
        Order order = orders.get(orderId);

        // Step 2: Handle unknown order
        if (order == null) {
            return Optional.empty();
        }

        // Step 3: Check ownership or administrator privilege
        boolean ownsOrder =
                order.ownerUserId().equals(authenticatedUserId);

        if (!ownsOrder && !isAdmin) {
            throw new OrderAccessDeniedException(
                    "You are not authorized to access this order."
            );
        }

        // Step 4: Return authorized order
        return Optional.of(order);
    }
}
