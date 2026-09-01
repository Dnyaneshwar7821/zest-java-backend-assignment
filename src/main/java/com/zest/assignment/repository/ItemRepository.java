package com.zest.assignment.repository;

import com.zest.assignment.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByProductId(Long productId);

    Optional<Item> findByIdAndProductId(Long id, Long productId);

    boolean existsByProductId(Long productId);

    void deleteByProductId(Long productId);
}
