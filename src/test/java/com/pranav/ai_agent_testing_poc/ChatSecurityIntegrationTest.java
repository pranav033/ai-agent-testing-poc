
package com.pranav.ai_agent_testing_poc;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ChatSecurityIntegrationTest {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

    @Value("${local.server.port}")
    private int port;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String aliceToken;
    private String bobToken;
    private String adminToken;

    @BeforeEach
    void authenticateTestUsers() throws Exception {

        aliceToken = login("alice", "alice123");
        bobToken = login("bob", "bob123");
        adminToken = login("admin", "admin123");
    }

    // ------------------------------------------------
    // 1. Authentication tests
    // ------------------------------------------------

    @Test
    void shouldRejectChatRequestWithoutJwt() throws Exception {

        HttpResponse<String> response = sendChat(
                "missing-token-session",
                "What is the status of order 101?",
                null
        );

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldRejectInvalidJwt() throws Exception {

        HttpResponse<String> response = sendChat(
                "invalid-token-session",
                "What is the status of order 101?",
                "this-is-not-a-valid-jwt"
        );

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldRejectInvalidLoginCredentials() throws Exception {

        String requestBody = """
                {
                    "username": "alice",
                    "password": "wrong-password"
                }
                """;

        HttpResponse<String> response = post(
                "/api/auth/login",
                requestBody,
                null
        );

        assertEquals(401, response.statusCode());
    }

    // ------------------------------------------------
    // 2. JWT security edge-case tests
    // ------------------------------------------------

    @Test
    void shouldRejectExpiredJwt() throws Exception {

        String expiredToken = createExpiredAliceToken();

        HttpResponse<String> response = sendChat(
                "expired-jwt-session",
                "What is the status of order 101?",
                expiredToken
        );

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldRejectTamperedJwt() throws Exception {

        String[] tokenParts = aliceToken.split("\\.");

        assertEquals(
                3,
                tokenParts.length,
                "A valid JWT must contain three parts"
        );

        // Modify the signature without changing the JWT header or payload.
        String signature = tokenParts[2];

        String modifiedSignature =
                (signature.charAt(0) == 'A' ? 'B' : 'A')
                        + signature.substring(1);

        String tamperedToken =
                tokenParts[0]
                        + "."
                        + tokenParts[1]
                        + "."
                        + modifiedSignature;

        HttpResponse<String> response = sendChat(
                "tampered-jwt-session",
                "What is the status of order 101?",
                tamperedToken
        );

        assertEquals(401, response.statusCode());
    }

    // ------------------------------------------------
    // 3. Alice authorization tests
    // ------------------------------------------------

    @Test
    void shouldAllowAliceToAccessHerOwnOrder() throws Exception {

        HttpResponse<String> response = sendChat(
                "alice-own-order-" + UUID.randomUUID(),
                "What is the status of order 101?",
                aliceToken
        );

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains(
                "Order 101 is currently shipped."
        ));

        assertTrue(response.body().contains(
                "\"taskCompleted\":true"
        ));
    }

    @Test
    void shouldDenyAliceAccessToBobsOrder() throws Exception {

        String sessionId = "alice-unauthorized-" + UUID.randomUUID();

        HttpResponse<String> unauthorizedResponse = sendChat(
                sessionId,
                "What is the status of order 102?",
                aliceToken
        );

        assertEquals(403, unauthorizedResponse.statusCode());

        assertTrue(unauthorizedResponse.body().contains("FORBIDDEN"));

        // Unauthorized order must not enter Alice's conversation memory.
        HttpResponse<String> followUpResponse = sendChat(
                sessionId,
                "Where is it now?",
                aliceToken
        );

        assertEquals(200, followUpResponse.statusCode());

        assertTrue(followUpResponse.body().contains(
                "Which order are you referring to?"
        ));
    }

    // ------------------------------------------------
    // 4. Bob authorization tests
    // ------------------------------------------------

    @Test
    void shouldAllowBobToAccessHisOwnOrder() throws Exception {

        HttpResponse<String> response = sendChat(
                "bob-own-order-" + UUID.randomUUID(),
                "What is the status of order 102?",
                bobToken
        );

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains(
                "Order 102 is currently out for delivery."
        ));

        assertTrue(response.body().contains(
                "\"taskCompleted\":true"
        ));
    }

    @Test
    void shouldDenyBobAccessToAlicesOrder() throws Exception {

        HttpResponse<String> response = sendChat(
                "bob-unauthorized-" + UUID.randomUUID(),
                "What is the status of order 101?",
                bobToken
        );

        assertEquals(403, response.statusCode());

        assertTrue(response.body().contains("FORBIDDEN"));
    }

    // ------------------------------------------------
    // 5. Admin authorization tests
    // ------------------------------------------------

    @Test
    void shouldAllowAdminToAccessBothUsersOrders() throws Exception {

        HttpResponse<String> aliceOrderResponse = sendChat(
                "admin-order-101-" + UUID.randomUUID(),
                "What is the status of order 101?",
                adminToken
        );

        assertEquals(200, aliceOrderResponse.statusCode());

        assertTrue(aliceOrderResponse.body().contains(
                "Order 101 is currently shipped."
        ));

        HttpResponse<String> bobOrderResponse = sendChat(
                "admin-order-102-" + UUID.randomUUID(),
                "What is the status of order 102?",
                adminToken
        );

        assertEquals(200, bobOrderResponse.statusCode());

        assertTrue(bobOrderResponse.body().contains(
                "Order 102 is currently out for delivery."
        ));
    }

    @Test
void shouldIgnoreClientSuppliedIdentityAndAdminFlags() throws Exception {

    String sessionId = "identity-spoofing-" + UUID.randomUUID();

    // Alice attempts to impersonate Bob and elevate herself to admin.
    String requestBody = """
            {
                "sessionId": "%s",
                "message": "What is the status of order 102?",
                "userId": "22222222-2222-4222-8222-222222222222",
                "isAdmin": true,
                "role": "ROLE_ADMIN"
            }
            """.formatted(sessionId);

    HttpResponse<String> response = post(
            "/api/chat",
            requestBody,
            aliceToken
    );

    // Alice must still be treated as Alice.
    assertEquals(403, response.statusCode());

    assertTrue(response.body().contains("FORBIDDEN"));
}

    // ------------------------------------------------
    // 6. Unknown order tests
    // ------------------------------------------------

    @Test
    void shouldReturnNotFoundMessageForUnknownOrder() throws Exception {

        HttpResponse<String> response = sendChat(
                "unknown-order-" + UUID.randomUUID(),
                "What is the status of order 999?",
                aliceToken
        );

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains(
                "I couldn't find reliable information for order 999."
        ));

        assertTrue(response.body().contains(
                "\"taskCompleted\":false"
        ));
    }

    // ------------------------------------------------
    // 7. Cross-user session isolation
    // ------------------------------------------------

    @Test
    void shouldIsolateConversationContextAcrossUsers()
            throws Exception {

        String sharedSessionId =
                "integration-shared-" + UUID.randomUUID();

        // Alice creates context for order 101.
        HttpResponse<String> aliceFirstResponse = sendChat(
                sharedSessionId,
                "What is the status of order 101?",
                aliceToken
        );

        assertEquals(200, aliceFirstResponse.statusCode());

        assertTrue(aliceFirstResponse.body().contains(
                "Order 101 is currently shipped."
        ));

        // Bob cannot inherit Alice's conversation context.
        HttpResponse<String> bobInitialResponse = sendChat(
                sharedSessionId,
                "Where is it now?",
                bobToken
        );

        assertEquals(200, bobInitialResponse.statusCode());

        assertTrue(bobInitialResponse.body().contains(
                "Which order are you referring to?"
        ));

        // Bob creates independent context for order 102.
        HttpResponse<String> bobOrderResponse = sendChat(
                sharedSessionId,
                "What is the status of order 102?",
                bobToken
        );

        assertEquals(200, bobOrderResponse.statusCode());

        assertTrue(bobOrderResponse.body().contains(
                "Order 102 is currently out for delivery."
        ));

        // Alice must still remember order 101.
        HttpResponse<String> aliceFollowUp = sendChat(
                sharedSessionId,
                "Where is it now?",
                aliceToken
        );

        assertEquals(200, aliceFollowUp.statusCode());

        assertTrue(aliceFollowUp.body().contains(
                "Order 101 is currently shipped."
        ));

        // Bob must still remember order 102.
        HttpResponse<String> bobFollowUp = sendChat(
                sharedSessionId,
                "Where is it now?",
                bobToken
        );

        assertEquals(200, bobFollowUp.statusCode());

        assertTrue(bobFollowUp.body().contains(
                "Order 102 is currently out for delivery."
        ));
    }

    // ------------------------------------------------
    // 8. JWT helper methods
    // ------------------------------------------------

    private String createExpiredAliceToken() {

        SecretKey signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );

        Instant now = Instant.now();

        return Jwts.builder()
                .subject("11111111-1111-4111-8111-111111111111")
                .claim("username", "alice")
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(signingKey)
                .compact();
    }

    // ------------------------------------------------
    // 9. HTTP helper methods
    // ------------------------------------------------

    private String login(
            String username,
            String password) throws Exception {

        String requestBody = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        HttpResponse<String> response = post(
                "/api/auth/login",
                requestBody,
                null
        );

        assertEquals(
                200,
                response.statusCode(),
                "Login failed for " + username + ": " + response.body()
        );

        Matcher matcher = TOKEN_PATTERN.matcher(response.body());

        assertTrue(
                matcher.find(),
                "Access token missing from login response for " + username
        );

        return matcher.group(1);
    }

    private HttpResponse<String> sendChat(
            String sessionId,
            String message,
            String token) throws Exception {

        String requestBody = """
                {
                    "sessionId": "%s",
                    "message": "%s"
                }
                """.formatted(sessionId, message);

        return post("/api/chat", requestBody, token);
    }

    private HttpResponse<String> post(
            String path,
            String requestBody,
            String token) throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port + path
                ))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        return httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
