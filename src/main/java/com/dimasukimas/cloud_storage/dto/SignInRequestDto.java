package com.dimasukimas.cloud_storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignInRequestDto(

        @NotBlank(message = "Login must not be blank")
        @Size(min = 5, max = 20, message = "Login must be between 5 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$", message = "Login can contain only latin letters and digits")
        String username,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>\\[\\]/`~+=\\-_'\\\\]*$",
                message = "Password can contain only latin letters, digits, and special characters")
        String password) {
}
