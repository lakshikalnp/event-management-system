package com.example.eventmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDto {

    @Email(message = "Invalid email format")
    @NotBlank(message = "is required")
    private String email;

    @NotBlank(message = "is required")
    private String password;
}
