package com.zest.assignment.service;

import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.entity.Role;
import com.zest.assignment.entity.User;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializerService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerService.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedSampleProducts();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@zestindiait.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                    .build();
            userRepository.save(admin);
            log.info("Initialized default administrator account: admin / Admin@123");
        }

        if (!userRepository.existsByUsername("user")) {
            User normalUser = User.builder()
                    .username("user")
                    .email("user@zestindiait.com")
                    .password(passwordEncoder.encode("User@123"))
                    .roles(Set.of(Role.ROLE_USER))
                    .build();
            userRepository.save(normalUser);
            log.info("Initialized default standard account: user / User@123");
        }
    }

    private void seedSampleProducts() {
        if (productRepository.count() == 0) {
            Product sample1 = Product.builder()
                    .productName("High-Performance Server Blade")
                    .build();

            sample1.addItem(Item.builder().quantity(10).build());
            sample1.addItem(Item.builder().quantity(25).build());

            productRepository.save(sample1);

            Product sample2 = Product.builder()
                    .productName("Enterprise Database Cluster")
                    .build();

            sample2.addItem(Item.builder().quantity(5).build());

            productRepository.save(sample2);

            log.info("Initialized sample product and item seed data.");
        }
    }
}
