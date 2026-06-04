package com.workforce.wms.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.workforce.wms.dto.report.MonthlyReportDetailResponse;
import com.workforce.wms.dto.workentry.WorkEntryResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MonthlyReportPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generate(MonthlyReportDetailResponse report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Monthly Work Report", titleFont));
            document.add(new Paragraph(" "));

            addRow(document, "Employee:", report.employeeName(), labelFont, valueFont);
            addRow(document, "Employee ID:", String.valueOf(report.employeeId()), labelFont, valueFont);
            addRow(document, "Year:", String.valueOf(report.year()), labelFont, valueFont);
            addRow(document, "Month:", String.valueOf(report.month()), labelFont, valueFont);
            addRow(document, "Status:", report.status().name(), labelFont, valueFont);
            addRow(document, "Total Minutes:", String.valueOf(report.totalMinutes()), labelFont, valueFont);
            addRow(document, "Total Hours:", formatDuration(report.totalMinutes()), labelFont, valueFont);

            if (report.submittedAt() != null) {
                addRow(document, "Submitted At:", formatDateTime(report.submittedAt()), labelFont, valueFont);
            }
            if (report.reviewedAt() != null) {
                addRow(document, "Reviewed At:", formatDateTime(report.reviewedAt()), labelFont, valueFont);
            }
            if (report.adminComment() != null && !report.adminComment().isBlank()) {
                addRow(document, "Admin Comment:", report.adminComment(), labelFont, valueFont);
            }

            document.add(new Paragraph(" "));

            if (report.entries() != null && !report.entries().isEmpty()) {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2f, 1.2f, 4f});

                addHeaderCell(table, "Date", headerFont);
                addHeaderCell(table, "Minutes", headerFont);
                addHeaderCell(table, "Description", headerFont);

                for (WorkEntryResponse entry : report.entries()) {
                    addCell(table, formatDate(entry.workDate()), cellFont);
                    addCell(table, String.valueOf(entry.minutes()), cellFont);
                    addCell(table, entry.description() != null ? entry.description() : "", cellFont);
                }

                document.add(table);
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }

        return out.toByteArray();
    }

    private void addRow(Document doc, String label, String value,
                        Font labelFont, Font valueFont) throws DocumentException {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + " ", labelFont));
        phrase.add(new Chunk(value, valueFont));
        doc.add(new Paragraph(phrase));
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String formatDate(LocalDate date) {
        return date.format(DATE_FMT);
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        return dateTime.format(DATETIME_FMT);
    }

    private String formatDuration(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours == 0) return minutes + "m";
        if (minutes == 0) return hours + "h";
        return hours + "h " + minutes + "m";
    }
}
