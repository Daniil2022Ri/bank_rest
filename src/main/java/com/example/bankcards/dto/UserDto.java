package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required", groups = Create.class)
    @Size(min = 8, message = "Password must be at least 8 characters", groups = Create.class)
    private String password;

    private String firstName;
    private String lastName;
    private User.Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;

    public interface Create {}
    public interface Update {}
}


