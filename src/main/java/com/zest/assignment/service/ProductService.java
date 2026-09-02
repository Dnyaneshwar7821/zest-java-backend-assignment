/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.service;

import com.zest.assignment.dto.request.ProductRequest;
import com.zest.assignment.dto.response.PagedResponse;
import com.zest.assignment.dto.response.ProductResponse;
import com.zest.assignment.dto.response.ProductSummaryResponse;

public interface ProductService {

    PagedResponse<ProductSummaryResponse> getAllProducts(
            int page,
            int size,
            String sortBy,
            String direction,
            String search
    );

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
