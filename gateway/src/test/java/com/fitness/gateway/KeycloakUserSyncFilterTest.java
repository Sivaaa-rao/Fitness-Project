package com.fitness.gateway;

import com.fitness.gateway.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class KeycloakUserSyncFilterTest {

    private final UserService userService = mock(UserService.class);
    private final KeycloakUserSyncFilter filter = new KeycloakUserSyncFilter(userService);

    @Test
    void filterContinuesWithoutUserSyncWhenAuthorizationHeaderIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/activities").build());
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        WebFilterChain chain = nextExchange -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(chainCalled).isTrue();
        verify(userService, never()).validateUser(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void filterSkipsUserSyncForAuthRoutes() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/register").build());
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        WebFilterChain chain = nextExchange -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(chainCalled).isTrue();
        verify(userService, never()).validateUser(org.mockito.ArgumentMatchers.anyString());
    }
}
