
package com.pranav.ai_agent_testing_poc;

import com.pranav.ai_agent_testing_poc.model.Order;
import com.pranav.ai_agent_testing_poc.security.DemoUserIds;
import com.pranav.ai_agent_testing_poc.service.OrderAccessDeniedException;
import com.pranav.ai_agent_testing_poc.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
    }

    @Test
    void shouldAllowUserToAccessOwnOrder() {

        Order order = orderService.findAccessibleOrder(
                DemoUserIds.ALICE,
                false,
                101L
        ).orElseThrow();

        assertEquals(101L, order.orderId());
        assertEquals(DemoUserIds.ALICE, order.ownerUserId());
        assertEquals("shipped", order.status());
    }

    @Test
    void shouldDenyUserAccessToAnotherUsersOrder() {

        assertThrows(
                OrderAccessDeniedException.class,
                () -> orderService.findAccessibleOrder(
                        DemoUserIds.ALICE,
                        false,
                        102L
                )
        );
    }

    @Test
    void shouldAllowAdminToAccessAnotherUsersOrder() {

        Order order = orderService.findAccessibleOrder(
                DemoUserIds.ADMIN,
                true,
                101L
        ).orElseThrow();

        assertEquals(101L, order.orderId());
        assertEquals(DemoUserIds.ALICE, order.ownerUserId());
    }

    @Test
    void shouldReturnEmptyForUnknownOrder() {

        var result = orderService.findAccessibleOrder(
                DemoUserIds.ALICE,
                false,
                999L
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldAllowBobToAccessHisOwnOrder() {

        Order order = orderService.findAccessibleOrder(
                DemoUserIds.BOB,
                false,
                102L
        ).orElseThrow();

        assertEquals(102L, order.orderId());
        assertEquals(DemoUserIds.BOB, order.ownerUserId());
        assertEquals("out for delivery", order.status());
    }
}
