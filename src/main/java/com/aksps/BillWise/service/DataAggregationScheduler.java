package com.aksps.BillWise.service;

import com.aksps.BillWise.model.InvoiceItem;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.model.SalesData;
import com.aksps.BillWise.repository.InvoiceRepository;
import com.aksps.BillWise.repository.SalesDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataAggregationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataAggregationScheduler.class);

    private final InvoiceRepository invoiceRepository;
    private final SalesDataRepository salesDataRepository;

    public DataAggregationScheduler(InvoiceRepository invoiceRepository,
                                    SalesDataRepository salesDataRepository) {
        this.invoiceRepository = invoiceRepository;
        this.salesDataRepository = salesDataRepository;
    }

    @Scheduled(fixedRate = 86400000)  // every 24 hours
    @Transactional
    public void aggregateSalesData() {

        logger.info("Running Monthly Sales Aggregation Job...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 1. Fetch invoice items from last 30 days
        List<InvoiceItem> recentSalesItems = invoiceRepository.findAll().stream()
                .flatMap(invoice -> invoice.getItems().stream())
                .filter(item -> item.getInvoice().getInvoiceDate().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        if (recentSalesItems.isEmpty()) {
            logger.info("No new sales in last 30 days. Skipping.");
            return;
        }

        // 2. Aggregate by Product + Month (store month as LocalDate = first day of month)
        Map<ProductMonthKey, Integer> aggregatedData = recentSalesItems.stream()
                .collect(Collectors.groupingBy(
                        item -> new ProductMonthKey(
                                item.getProduct(),
                                // convert invoice datetime to LocalDate representing first day of that month
                                item.getInvoice().getInvoiceDate().toLocalDate().withDayOfMonth(1)
                        ),
                        Collectors.summingInt(InvoiceItem::getQuantitySold)
                ));

        int updates = 0;
        int creates = 0;

        for (Map.Entry<ProductMonthKey, Integer> entry : aggregatedData.entrySet()) {

            Product product = entry.getKey().product();
            LocalDate monthDate = entry.getKey().month();

            Integer unitsSold = entry.getValue();

            SalesData salesData = salesDataRepository
                    .findByProductAndMonth(product, monthDate)
                    .orElse(null);

            if (salesData == null) {
                // CREATE new monthly record
                salesData = new SalesData();
                salesData.setProduct(product);
                salesData.setMonth(monthDate);

                creates++;
            } else {
                updates++;
            }

            salesData.setTotalUnitsSold(unitsSold);

            salesDataRepository.save(salesData);
        }

        logger.info(
                "Aggregation completed: {} created, {} updated, {} items processed.",
                creates, updates, recentSalesItems.size()
        );
    }

    /**
     * Helper key for stream grouping (Product + Month)
     */
    private record ProductMonthKey(Product product, LocalDate month) { }
}
