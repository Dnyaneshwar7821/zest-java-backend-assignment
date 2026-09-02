/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Login Request")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(example = "admin", description = "Registered username or email")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(example = "Admin@123", description = "Account password")
    private String password;
}
