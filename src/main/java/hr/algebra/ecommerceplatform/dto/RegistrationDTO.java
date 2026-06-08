package hr.algebra.ecommerceplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 100)
    private String password;
}
