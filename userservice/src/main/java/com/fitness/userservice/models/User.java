package com.fitness.userservice.models;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String keycloakId;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String firstName;
    private String lastName;

    private UserRole role = UserRole.USER;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
