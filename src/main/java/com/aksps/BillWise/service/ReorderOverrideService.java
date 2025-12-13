package com.aksps.BillWise.service;

import com.aksps.BillWise.model.ReorderOverride;
import com.aksps.BillWise.repository.ReorderOverrideRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReorderOverrideService {

    private final ReorderOverrideRepository repo;

    public ReorderOverrideService(ReorderOverrideRepository repo) {
        this.repo = repo;
    }

    public ReorderOverride saveOverride(ReorderOverride ov) {
        return repo.save(ov);
    }

    public List<ReorderOverride> getOverridesForProduct(Long productId) {
        return repo.findByProductIdOrderByCreatedAtDesc(productId);
    }
}
