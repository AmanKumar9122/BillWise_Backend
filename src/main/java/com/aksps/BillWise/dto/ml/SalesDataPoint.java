package com.aksps.BillWise.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesDataPoint {
    private String date;  // YYYY-MM-DD
    private Integer unitsSold;
}
