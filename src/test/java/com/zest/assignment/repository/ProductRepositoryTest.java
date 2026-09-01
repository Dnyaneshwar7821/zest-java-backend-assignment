package com.zest.assignment.repository;

import com.zest.assignment.config.JpaAuditingConfig;
import com.zest.assignment.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Should save product with JPA auditing and find by ID")
    void testSaveAndFindById() {
        Product product = Product.builder()
                .productName("Ultrabook Pro")
                .build();

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedBy()).isNotNull();
        assertThat(saved.getCreatedOn()).isNotNull();

        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getProductName()).isEqualTo("Ultrabook Pro");
    }

    @Test
    @DisplayName("Should search product by name ignoring case")
    void testFindByProductNameContainingIgnoreCase() {
        productRepository.save(Product.builder().productName("Gaming Laptop RTX").build());
        productRepository.save(Product.builder().productName("Office Laptop Standard").build());

        Page<Product> results = productRepository.findByProductNameContainingIgnoreCase("laptop", PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(2);
    }
}
