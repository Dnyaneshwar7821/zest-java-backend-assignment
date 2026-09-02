/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.service;

import com.zest.assignment.dto.request.ProductRequest;
import com.zest.assignment.dto.response.PagedResponse;
import com.zest.assignment.dto.response.ProductResponse;
import com.zest.assignment.dto.response.ProductSummaryResponse;
import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.DuplicateResourceException;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AsyncAuditService asyncAuditService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Sample Laptop")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        productRequest = ProductRequest.builder()
                .productName("Sample Laptop")
                .build();
    }

    @Test
    @DisplayName("Create Product - Success")
    void testCreateProduct_Success() {
        when(productRepository.existsByProductNameIgnoreCase("Sample Laptop")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(productRequest);

        assertThat(response).isNotNull();
        assertThat(response.getProductName()).isEqualTo("Sample Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
        verify(asyncAuditService, times(1)).logAudit(eq("PRODUCT_CREATED"), eq("Product"), eq(1L), any(), anyString());
    }

    @Test
    @DisplayName("Create Product - Duplicate Name Throws DuplicateResourceException")
    void testCreateProduct_DuplicateName_ThrowsException() {
        when(productRepository.existsByProductNameIgnoreCase("Sample Laptop")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Get Product By ID - Success")
    void testGetProductById_Success() {
        product.addItem(Item.builder().id(10L).quantity(5).product(product).build());
        when(productRepository.findByIdWithItems(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Get Product By ID - Not Found Throws ResourceNotFoundException")
    void testGetProductById_NotFound_ThrowsException() {
        when(productRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Get All Products - Paginated")
    void testGetAllProducts_Success() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductSummaryResponse> response = productService.getAllProducts(0, 10, "id", "asc", null);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get All Products - With Search Filter")
    void testGetAllProducts_WithSearch() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByProductNameContainingIgnoreCase(eq("Laptop"), any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductSummaryResponse> response = productService.getAllProducts(0, 10, "id", "asc", "Laptop");

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(productRepository, times(1)).findByProductNameContainingIgnoreCase(eq("Laptop"), any(Pageable.class));
    }

    @Test
    @DisplayName("Update Product - Success")
    void testUpdateProduct_Success() {
        ProductRequest updateReq = ProductRequest.builder().productName("Updated Laptop").build();
        Product updatedProduct = Product.builder()
                .id(1L)
                .productName("Updated Laptop")
                .modifiedBy("admin")
                .modifiedOn(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByProductNameIgnoreCaseAndIdNot("Updated Laptop", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponse response = productService.updateProduct(1L, updateReq);

        assertThat(response).isNotNull();
        assertThat(response.getProductName()).isEqualTo("Updated Laptop");
    }

    @Test
    @DisplayName("Delete Product - Success")
    void testDeleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(product);
        verify(asyncAuditService, times(1)).logAudit(eq("PRODUCT_DELETED"), eq("Product"), eq(1L), any(), anyString());
    }
}
