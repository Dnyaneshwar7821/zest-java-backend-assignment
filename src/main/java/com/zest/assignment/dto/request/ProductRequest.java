/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product Creation / Update Request")
public class ProductRequest {

    @NotBlank(message = "Product name is required and cannot be blank")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    @Schema(example = "Enterprise Cloud Server X1", description = "Name of the product")
    private String productName;
}
