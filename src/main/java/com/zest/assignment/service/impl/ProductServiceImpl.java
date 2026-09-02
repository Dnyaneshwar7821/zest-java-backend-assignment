/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.service.impl;

import com.zest.assignment.dto.request.ProductRequest;
import com.zest.assignment.dto.response.ItemResponse;
import com.zest.assignment.dto.response.PagedResponse;
import com.zest.assignment.dto.response.ProductResponse;
import com.zest.assignment.dto.response.ProductSummaryResponse;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.DuplicateResourceException;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.security.SecurityUtils;
import com.zest.assignment.service.AsyncAuditService;
import com.zest.assignment.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AsyncAuditService asyncAuditService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductSummaryResponse> getAllProducts(
            int page,
            int size,
            String sortBy,
            String direction,
            String search) {

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                StringUtils.hasText(sortBy) ? sortBy : "id"
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage;

        if (StringUtils.hasText(search)) {
            productPage = productRepository.findByProductNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        Page<ProductSummaryResponse> mappedPage = productPage.map(this::mapToSummaryResponse);
        return PagedResponse.from(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String trimmedName = request.getProductName().trim();

        if (productRepository.existsByProductNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("A product with name '" + trimmedName + "' already exists!");
        }

        Product product = Product.builder()
                .productName(trimmedName)
                .build();

        Product savedProduct = productRepository.save(product);

        asyncAuditService.logAudit(
                "PRODUCT_CREATED",
                "Product",
                savedProduct.getId(),
                savedProduct.getCreatedBy(),
                "Created product: " + savedProduct.getProductName()
        );

        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        String trimmedName = request.getProductName().trim();

        if (productRepository.existsByProductNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException("Another product with name '" + trimmedName + "' already exists!");
        }

        product.setProductName(trimmedName);
        Product updatedProduct = productRepository.save(product);

        asyncAuditService.logAudit(
                "PRODUCT_UPDATED",
                "Product",
                updatedProduct.getId(),
                updatedProduct.getModifiedBy(),
                "Updated product name to: " + updatedProduct.getProductName()
        );

        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        String productName = product.getProductName();
        String currentUser = SecurityUtils.getCurrentUsername().orElse("SYSTEM");

        productRepository.delete(product);

        asyncAuditService.logAudit(
                "PRODUCT_DELETED",
                "Product",
                id,
                currentUser,
                "Deleted product: " + productName
        );
    }

    private ProductSummaryResponse mapToSummaryResponse(Product product) {
        return ProductSummaryResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .createdBy(product.getCreatedBy())
                .createdOn(product.getCreatedOn())
                .modifiedBy(product.getModifiedBy())
                .modifiedOn(product.getModifiedOn())
                .itemCount(product.getItems() != null ? product.getItems().size() : 0)
                .build();
    }

    private ProductResponse mapToProductResponse(Product product) {
        List<ItemResponse> itemResponses = product.getItems() != null
                ? product.getItems().stream()
                .map(item -> ItemResponse.builder()
                        .id(item.getId())
                        .productId(product.getId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .createdBy(product.getCreatedBy())
                .createdOn(product.getCreatedOn())
                .modifiedBy(product.getModifiedBy())
                .modifiedOn(product.getModifiedOn())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .build();
    }
}
