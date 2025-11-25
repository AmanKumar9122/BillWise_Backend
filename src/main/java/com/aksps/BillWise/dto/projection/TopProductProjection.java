package com.aksps.BillWise.dto.projection;

import java.math.BigDecimal;

public interface TopProductProjection {

    Long getProductId();
    String getProductName();
    String getSku();
    String getUnitType();
    Integer getCurrentStock();
    BigDecimal getTotalRevenue();
}
