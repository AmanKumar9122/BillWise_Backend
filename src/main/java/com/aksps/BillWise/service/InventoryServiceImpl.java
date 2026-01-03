package com.aksps.BillWise.service;

import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Override
    public int getAvailableStock(Long productId) {
        // TODO: wire real product/inventory repository to return actual stock.
        // Placeholder implementation returns 0 so reorder logic still works safely.
        return 0;
    }
}

