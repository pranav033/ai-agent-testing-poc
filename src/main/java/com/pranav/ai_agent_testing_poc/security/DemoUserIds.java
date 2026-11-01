
package com.pranav.ai_agent_testing_poc.security;

import java.util.UUID;

public final class DemoUserIds {

    public static final UUID ALICE =
            UUID.fromString("11111111-1111-4111-8111-111111111111");

    public static final UUID BOB =
            UUID.fromString("22222222-2222-4222-8222-222222222222");

    public static final UUID ADMIN =
            UUID.fromString("33333333-3333-4333-8333-333333333333");

    private DemoUserIds() {
        // Prevent instantiation
    }
}
