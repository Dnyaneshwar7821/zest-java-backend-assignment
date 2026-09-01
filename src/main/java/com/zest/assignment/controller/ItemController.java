package com.zest.assignment.controller;

import com.zest.assignment.dto.request.ItemUpdateRequest;
import com.zest.assignment.dto.response.ApiResponse;
import com.zest.assignment.dto.response.ItemResponse;
import com.zest.assignment.dto.response.MessageResponse;
import com.zest.assignment.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Endpoints for granular item management")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves an individual item by its unique ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(
            @Parameter(description = "Item ID", example = "1")
            @PathVariable Long id) {
        ItemResponse response = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Item retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of an existing item.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @Parameter(description = "Item ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ItemUpdateRequest request) {
        ItemResponse response = itemService.updateItemQuantity(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Item updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an item (Admin only)", description = "Deletes an item from the database. Requires ROLE_ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied: requires ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<ApiResponse<MessageResponse>> deleteItem(
            @Parameter(description = "Item ID", example = "1")
            @PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success(
                MessageResponse.builder().message("Item deleted successfully with ID: " + id).build(),
                "Item deleted successfully"
        ));
    }
}
