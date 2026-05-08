package com.fitness.userservice.services;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerCreatesNewUserWithoutExposingPassword() {
        RegisterRequest request = registerRequest();
        User savedUser = user("id-1", "kc-1", request.getEmail(), request.getPassword());

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(repository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertThat(response.getId()).isEqualTo("id-1");
        assertThat(response.getKeycloakId()).isEqualTo("kc-1");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getPassword()).isNull();
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getEmail().equals(request.getEmail())
                        && user.getKeycloakId().equals(request.getKeycloakId())
                        && user.getPassword().equals(request.getPassword())));
    }

    @Test
    void registerReturnsExistingUserWithoutExposingPassword() {
        RegisterRequest request = registerRequest();
        User existingUser = user("id-2", "kc-2", request.getEmail(), "stored-secret");

        when(repository.existsByEmail(request.getEmail())).thenReturn(true);
        when(repository.findByEmail(request.getEmail())).thenReturn(existingUser);

        UserResponse response = userService.register(request);

        assertThat(response.getId()).isEqualTo("id-2");
        assertThat(response.getKeycloakId()).isEqualTo("kc-2");
        assertThat(response.getPassword()).isNull();
    }

    @Test
    void getUserProfileLooksUpByKeycloakId() {
        User user = user("id-3", "kc-3", "profile@example.com", "secret");
        when(repository.findByKeycloakId("kc-3")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserProfile("kc-3");

        assertThat(response.getId()).isEqualTo("id-3");
        assertThat(response.getKeycloakId()).isEqualTo("kc-3");
        assertThat(response.getPassword()).isNull();
    }

    @Test
    void getUserProfileThrowsWhenUserIsMissing() {
        when(repository.findByKeycloakId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void existByUserIdDelegatesToRepository() {
        when(repository.existsByKeycloakId("kc-4")).thenReturn(true);

        assertThat(userService.existByUserId("kc-4")).isTrue();
    }

    private static RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setKeycloakId("kc-1");
        request.setFirstName("Test");
        request.setLastName("User");
        return request;
    }

    private static User user(String id, String keycloakId, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setKeycloakId(keycloakId);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
