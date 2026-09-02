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
@Schema(description = "Token Refresh Request")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(example = "3c24b2b1-5e8a-40a2-964d-3b7c25e83912", description = "Current valid refresh token")
    private String refreshToken;
}
