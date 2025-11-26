package com.aksps.BillWise.dto.request;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public record InvoiceFilterRequest(
        String customerName,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {}
