package com.aksps.BillWise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private String contactNumber;
    private String email;
    private String gstNumber;
}