package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.ForecastResultRequest;
import com.aksps.BillWise.model.ForecastResult;
import com.aksps.BillWise.repository.ForecastResultRepository;
import org.springframework.stereotype.Service;

@Service
public class ForecastResultService {

    private final ForecastResultRepository repo;
    private final InventoryService inventoryService;
    private final ReorderService reorderService;

    public ForecastResultService(ForecastResultRepository repo,
                                 InventoryService inventoryService,
                                 ReorderService reorderService) {
        this.repo = repo;
        this.inventoryService = inventoryService;
        this.reorderService = reorderService;
    }

    public void save(ForecastResultRequest req) {

        repo.deleteByProductIdAndForecastMonth(
                req.getProductId(),
                req.getForecastMonth()
        );

        ForecastResult fr = new ForecastResult();
        fr.setProductId(req.getProductId());
        fr.setForecastMonth(req.getForecastMonth());
        fr.setPredictedUnits(req.getPredictedUnits());
        fr.setPredictedRevenue(req.getPredictedRevenue());
        fr.setModelType(req.getModelType());
        fr.setGeneratedAt(req.getGeneratedAt());

        ForecastResult saved = repo.save(fr);

        // Immediately evaluate reorder decision after saving forecast
        int currentStock = inventoryService.getAvailableStock(saved.getProductId());
        reorderService.evaluateReorder(saved, currentStock);
    }
}
