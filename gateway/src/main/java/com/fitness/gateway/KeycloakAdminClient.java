package com.fitness.gateway;


import com.fitness.gateway.user.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminClient {

    private final KeycloakAdminProperties props;

    private WebClient adminClient() {
        return WebClient.builder()
                .baseUrl(props.getServerUrl())
                .build();
    }

    private Mono<String> getAdminToken() {
        return adminClient().post()
                .uri("/realms/{realm}/protocol/openid-connect/token", props.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", props.getClientId())
                        .with("client_secret", props.getClientSecret()))
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> (String) body.get("access_token"));
    }

    public Mono<Void> createUser(String email, String password,
                                 String firstName, String lastName) {
        return getAdminToken()
                .flatMap(token -> {
                    Map<String, Object> payload = Map.of(
                            "username", email,
                            "email", email,
                            "firstName", firstName,
                            "lastName", lastName,
                            "enabled", true,
                            "emailVerified", true,
                            "credentials", new Object[]{
                                    Map.of(
                                            "type", "password",
                                            "value", password,
                                            "temporary", false
                                    )
                            }
                    );

                    return adminClient().post()
                            .uri("/admin/realms/{realm}/users", props.getRealm())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(payload)
                            .retrieve()
                            .onStatus(
                                    status -> status == HttpStatus.CONFLICT,
                                    response -> {
                                        log.warn("Keycloak returned 409 CONFLICT for user {}", email);
                                        return Mono.error(new UserAlreadyExistsException(
                                                "User with this email/username already exists"));
                                    }
                            )
                            .toBodilessEntity()
                            .then();
                });
    }
}

