package com.bookstore.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginRequestDto {
    @Email(message = "Email is not valid")
    @NotEmpty
    private String email;
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
