/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.assignment.dto.request.ItemRequest;
import com.zest.assignment.dto.request.ProductRequest;
import com.zest.assignment.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userToken = "Bearer " + jwtTokenProvider.generateTokenFromUsername("user", List.of("ROLE_USER"));
        adminToken = "Bearer " + jwtTokenProvider.generateTokenFromUsername("admin", List.of("ROLE_ADMIN", "ROLE_USER"));
    }

    @Test
    @DisplayName("GET /api/v1/products - Unauthorized without token returns 401")
    void testGetAllProducts_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("GET /api/v1/products - Authenticated returns 200 with paginated data")
    void testGetAllProducts_Authenticated_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @DisplayName("Full CRUD & Item Flow on Product")
    void testFullProductAndItemLifecycle() throws Exception {
        // Step 1: Create Product (authenticated user)
        ProductRequest createReq = ProductRequest.builder()
                .productName("MacBook Pro M3 Max")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.productName", is("MacBook Pro M3 Max")))
                .andExpect(jsonPath("$.data.createdBy", is("user")))
                .andReturn();

        Long productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Step 2: Get Product by ID
        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(productId.intValue())))
                .andExpect(jsonPath("$.data.productName", is("MacBook Pro M3 Max")));

        // Step 3: Add Items to the Product
        ItemRequest itemReq = ItemRequest.builder().quantity(15).build();
        mockMvc.perform(post("/api/v1/products/" + productId + "/items")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity", is(15)))
                .andExpect(jsonPath("$.data.productId", is(productId.intValue())));

        // Step 4: Get Items of the Product
        mockMvc.perform(get("/api/v1/products/" + productId + "/items")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].quantity", is(15)));

        // Step 5: Update Product Name
        ProductRequest updateReq = ProductRequest.builder()
                .productName("MacBook Pro M3 Max - 64GB")
                .build();

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName", is("MacBook Pro M3 Max - 64GB")))
                .andExpect(jsonPath("$.data.modifiedBy", is("user")));

        // Step 6: Delete Product as regular USER -> 403 Forbidden (RBAC Test)
        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());

        // Step 7: Delete Product as ADMIN -> 200 OK
        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Step 8: Verify product is deleted -> 404 Not Found
        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/products - Blank product name returns 400 Validation Error")
    void testCreateProduct_BlankName_Returns400() throws Exception {
        ProductRequest invalidReq = ProductRequest.builder()
                .productName("")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(1))));
    }
}
