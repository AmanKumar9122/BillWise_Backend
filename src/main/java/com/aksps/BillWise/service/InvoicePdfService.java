package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.InvoiceReportDetail;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

@Service
public class InvoicePdfService {

    public byte[] generatePdf(InvoiceReportDetail report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph(report.storeName()));
            document.add(new Paragraph("Address: " + report.storeAddress()));
            document.add(new Paragraph("Phone: " + report.storeContact()));
            document.add(new Paragraph(" "));

            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            document.add(new Paragraph("Customer Details", headerFont));
            document.add(new Paragraph("Name: " + report.customerName()));
            addOptionalLine(document, "Contact: ", report.customerContact());
            addOptionalLine(document, "Email: ", report.customerEmail());
            addOptionalLine(document, "GST: ", report.customerGst());
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Invoice Number: " + report.invoiceNumber()));
            document.add(new Paragraph("Date: " + report.invoiceDate()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{3, 1, 2, 2, 2});
            addTableHeader(table);
            addRows(table, report);
            document.add(table);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Subtotal: Rs. " + report.subTotal()));
            document.add(new Paragraph("Tax (" + report.taxRatePercent() + "%): Rs. " + report.totalTax()));
            document.add(new Paragraph("Discount: Rs. " + report.totalDiscount()));
            document.add(new Paragraph("Grand Total: Rs. " + report.grandTotal()));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addOptionalLine(Document document, String label, String value) {
        if (value != null && !value.isBlank()) {
            document.add(new Paragraph(label + value));
        }
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Item", "Qty", "Unit Price", "Discount", "Line Total")
                .forEach(column -> {
                    PdfPCell header = new PdfPCell(new Phrase(column));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, InvoiceReportDetail report) {
        report.items().forEach(item -> {
            table.addCell(item.productName());
            table.addCell(String.valueOf(item.quantity()));
            table.addCell("Rs. " + item.unitPrice());
            table.addCell("Rs. " + item.discount());
            table.addCell("Rs. " + item.lineTotal());
        });
    }
}
