package com.fitness.gateway.user;

import com.fitness.gateway.KeycloakAdminClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationControllerTest {

    private final KeycloakAdminClient keycloakAdminClient = mock(KeycloakAdminClient.class);
    private final RegistrationController controller = new RegistrationController(keycloakAdminClient);

    @Test
    void registerReturnsCreatedWhenKeycloakUserIsCreated() {
        RegisterRequest request = request();
        when(keycloakAdminClient.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.register(request))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(response.getBody()).isEqualTo("User registered. Please log in.");
                })
                .verifyComplete();
    }

    @Test
    void registerReturnsConflictWhenKeycloakUserAlreadyExists() {
        RegisterRequest request = request();
        when(keycloakAdminClient.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()))
                .thenReturn(Mono.error(new UserAlreadyExistsException("User already exists")));

        StepVerifier.create(controller.register(request))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(response.getBody()).isEqualTo("User already exists");
                })
                .verifyComplete();
    }

    private static RegisterRequest request() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        return request;
    }
}
