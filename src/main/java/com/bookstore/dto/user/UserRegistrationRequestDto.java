package com.bookstore.dto.user;

import com.bookstore.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@FieldMatch(
        field = "password",
        fieldMatch = "repeatPassword",
        message = "Passwords do not match!")
public class UserRegistrationRequestDto {
    @Email(message = "Email is not valid")
    @NotEmpty
    private String email;
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
    private String repeatPassword;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    private String shippingAddress;
}
