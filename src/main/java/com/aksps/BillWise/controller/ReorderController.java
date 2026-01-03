package com.aksps.BillWise.controller;

import com.aksps.BillWise.model.ReorderSuggestion;
import com.aksps.BillWise.repository.ReorderSuggestionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class ReorderController {

    private final ReorderSuggestionRepository repo;

    public ReorderController(ReorderSuggestionRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/reorder-suggestions")
    public List<ReorderSuggestion> getSuggestions() {
        return repo.findByStatus("PENDING");
    }
}

