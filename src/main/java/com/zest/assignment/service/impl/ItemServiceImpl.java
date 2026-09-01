package com.zest.assignment.service.impl;

import com.zest.assignment.dto.request.ItemRequest;
import com.zest.assignment.dto.request.ItemUpdateRequest;
import com.zest.assignment.dto.response.ItemResponse;
import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.ItemRepository;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.security.SecurityUtils;
import com.zest.assignment.service.AsyncAuditService;
import com.zest.assignment.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final AsyncAuditService asyncAuditService;

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return itemRepository.findByProductId(productId).stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return mapToItemResponse(item);
    }

    @Override
    @Transactional
    public ItemResponse addItemToProduct(Long productId, ItemRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Item item = Item.builder()
                .product(product)
                .quantity(request.getQuantity())
                .build();

        Item savedItem = itemRepository.save(item);

        String currentUser = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        asyncAuditService.logAudit(
                "ITEM_ADDED",
                "Item",
                savedItem.getId(),
                currentUser,
                "Added item with quantity " + savedItem.getQuantity() + " to product ID: " + productId
        );

        return mapToItemResponse(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse updateItemQuantity(Long id, ItemUpdateRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        int oldQuantity = item.getQuantity();
        item.setQuantity(request.getQuantity());
        Item updatedItem = itemRepository.save(item);

        String currentUser = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        asyncAuditService.logAudit(
                "ITEM_UPDATED",
                "Item",
                updatedItem.getId(),
                currentUser,
                "Updated item ID " + id + " quantity from " + oldQuantity + " to " + updatedItem.getQuantity()
        );

        return mapToItemResponse(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        Long productId = item.getProduct().getId();
        itemRepository.delete(item);

        String currentUser = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        asyncAuditService.logAudit(
                "ITEM_DELETED",
                "Item",
                id,
                currentUser,
                "Deleted item ID " + id + " from product ID: " + productId
        );
    }

    private ItemResponse mapToItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .build();
    }
}
