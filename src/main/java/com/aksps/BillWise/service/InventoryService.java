package com.aksps.BillWise.service;

public interface InventoryService {
    /**
     * Returns current available stock for the product. Implementations should
     * look up inventory/product tables. Returning 0 is safe if unknown.
     */
    int getAvailableStock(Long productId);
}

