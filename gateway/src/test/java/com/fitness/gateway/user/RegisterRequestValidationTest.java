package com.fitness.gateway.user;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void keycloakIdIsOptionalForSignupRequests() {
        RegisterRequest request = validRequest();
        request.setKeycloakId(null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void passwordIsRequiredAndMustBeLongEnough() {
        RegisterRequest request = validRequest();
        request.setPassword("123");

        assertThat(validator.validate(request))
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString())
                        .isEqualTo("password"));
    }

    private static RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        return request;
    }
}
