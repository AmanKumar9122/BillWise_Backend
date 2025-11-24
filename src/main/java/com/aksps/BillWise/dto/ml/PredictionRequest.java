package com.aksps.BillWise.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionRequest {
    private String productSku;
    private List<SalesDataPoint> salesHistory;
}
