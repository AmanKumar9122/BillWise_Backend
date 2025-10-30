package com.aksps.BillWise.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(min = 10, max = 10)
    private String contactNumber;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 50)
    private String gstNumber;
}
