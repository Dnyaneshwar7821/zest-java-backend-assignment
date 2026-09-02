/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldErrorDetail {
    private String field;
    private Object rejectedValue;
    private String message;
}
