package com.aksps.BillWise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReplenishmentSuggestion {
    private Long productId;
    private int currentStock;
    private int predictedDemandNextMonth;
    private int suggestedReorderQuantity;
    private String suggestedReorderDate;
    private int daysUntilStockout;
    private int stockoutRiskScore; // 0–100%
}
