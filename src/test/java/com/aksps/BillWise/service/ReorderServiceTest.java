package com.aksps.BillWise.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class ReorderServiceTest {

    @Test
    public void testSuggestReorder_basic() {
        ReorderService svc = new ReorderService(Mockito.mock(com.aksps.BillWise.repository.ReorderSuggestionRepository.class));
        // onHand=5, pending=0, pack=10, leadTime=7 days, forecastedDemandInLeadTime=20, serviceLevel=0.95
        ReorderService.Suggestion s = svc.suggestReorder(5, 0, 10, 7.0, 20, 0.95);
        assertNotNull(s);
        assertTrue(s.suggestedQty >= 0);
        // suggested qty should be multiple of pack size
        assertEquals(0, s.suggestedQty % 10);
    }
}


