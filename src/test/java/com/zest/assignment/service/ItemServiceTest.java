/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.service;

import com.zest.assignment.dto.request.ItemRequest;
import com.zest.assignment.dto.request.ItemUpdateRequest;
import com.zest.assignment.dto.response.ItemResponse;
import com.zest.assignment.entity.Item;
import com.zest.assignment.entity.Product;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.ItemRepository;
import com.zest.assignment.repository.ProductRepository;
import com.zest.assignment.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AsyncAuditService asyncAuditService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Product product;
    private Item item;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).productName("Gadget").build();
        item = Item.builder().id(10L).quantity(5).product(product).build();
    }

    @Test
    @DisplayName("Get Items By Product ID - Success")
    void testGetItemsByProductId_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByProductId(1L)).thenReturn(List.of(item));

        List<ItemResponse> responses = itemService.getItemsByProductId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Add Item To Product - Success")
    void testAddItemToProduct_Success() {
        ItemRequest req = ItemRequest.builder().quantity(12).build();
        Item savedItem = Item.builder().id(11L).quantity(12).product(product).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemResponse response = itemService.addItemToProduct(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getQuantity()).isEqualTo(12);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("Update Item Quantity - Success")
    void testUpdateItemQuantity_Success() {
        ItemUpdateRequest req = ItemUpdateRequest.builder().quantity(20).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = itemService.updateItemQuantity(10L, req);

        assertThat(response).isNotNull();
        assertThat(item.getQuantity()).isEqualTo(20);
    }

    @Test
    @DisplayName("Delete Item - Success")
    void testDeleteItem_Success() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).delete(item);

        itemService.deleteItem(10L);

        verify(itemRepository, times(1)).delete(item);
    }

    @Test
    @DisplayName("Delete Item - Not Found Throws Exception")
    void testDeleteItem_NotFound_ThrowsException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
