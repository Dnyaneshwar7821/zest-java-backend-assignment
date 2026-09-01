package com.zest.assignment.service;

import com.zest.assignment.dto.request.ItemRequest;
import com.zest.assignment.dto.request.ItemUpdateRequest;
import com.zest.assignment.dto.response.ItemResponse;

import java.util.List;

public interface ItemService {

    List<ItemResponse> getItemsByProductId(Long productId);

    ItemResponse getItemById(Long id);

    ItemResponse addItemToProduct(Long productId, ItemRequest request);

    ItemResponse updateItemQuantity(Long id, ItemUpdateRequest request);

    void deleteItem(Long id);
}
