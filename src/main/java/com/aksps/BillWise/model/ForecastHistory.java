package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "forecast_history")
@Data
public class ForecastHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Integer months;

    private LocalDate generatedAt;

    private Double predictedTotalRevenue;

    @Lob
    private String dailyPredictionsJson;  // store full list as JSON string
}
