package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.InvoiceReportDetail;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

@Service
public class InvoicePdfService {

    public byte[] generatePdf(InvoiceReportDetail report) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);

            document.open();

            // ====== HEADER ======
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ")); // Spacer

            // Company Info
            document.add(new Paragraph("BillWise Store"));
            document.add(new Paragraph("Address: Demo Market, Indore"));
            document.add(new Paragraph("Phone: +91 99999 99999"));
            document.add(new Paragraph(" "));

            // ===== CUSTOMER DETAILS =====
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            document.add(new Paragraph("Customer Details", headerFont));
            document.add(new Paragraph("Name: " + report.customerName()));
            if (report.customerContact() != null)
                document.add(new Paragraph("Contact: " + report.customerContact()));
            if (report.customerEmail() != null)
                document.add(new Paragraph("Email: " + report.customerEmail()));
            if (report.customerGst() != null)
                document.add(new Paragraph("GST: " + report.customerGst()));
            document.add(new Paragraph(" "));

            // ===== INVOICE INFO =====
            document.add(new Paragraph("Invoice Number: " + report.invoiceNumber()));
            document.add(new Paragraph("Date: " + report.invoiceDate()));
            document.add(new Paragraph(" "));

            // ===== ITEMS TABLE =====
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{3, 1, 2, 2, 2});

            addTableHeader(table);
            addRows(table, report);

            document.add(table);
            document.add(new Paragraph(" "));

            // ===== TOTALS =====
            document.add(new Paragraph("Subtotal: ₹" + report.subTotal()));
            document.add(new Paragraph("Tax (" + report.taxRatePercent() + "%): ₹" + report.totalTax()));
            document.add(new Paragraph("Discount: ₹" + report.totalDiscount()));
            document.add(new Paragraph("Grand Total: ₹" + report.grandTotal()));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Item", "Qty", "Unit Price", "Discount", "Line Total")
                .forEach(column -> {
                    PdfPCell header = new PdfPCell();
                    header.setPhrase(new Phrase(column));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, InvoiceReportDetail report) {
        report.items().forEach(item -> {
            table.addCell(item.productName());
            table.addCell(String.valueOf(item.quantity()));
            table.addCell("₹" + item.unitPrice());
            table.addCell("₹" + item.discount());
            table.addCell("₹" + item.lineTotal());
        });
    }
}
