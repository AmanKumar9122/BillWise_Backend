package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.ForecastMetricRequest;
import com.aksps.BillWise.model.ForecastMetric;
import com.aksps.BillWise.repository.ForecastMetricRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ForecastMetricService {

    private final ForecastMetricRepository repo;

    public ForecastMetricService(ForecastMetricRepository repo) {
        this.repo = repo;
    }

    public void save(ForecastMetricRequest req) {
        ForecastMetric m = new ForecastMetric();
        m.setProductId(req.getProductId());
        m.setMae(req.getMae());
        m.setRmse(req.getRmse());
        m.setModelType(req.getModelType());
        // default evaluatedAt to now() if not provided
        m.setEvaluatedAt(req.getEvaluatedAt() != null ? req.getEvaluatedAt() : LocalDateTime.now());

        repo.save(m);
    }
}
