package com.bank.light.services;

import com.bank.light.domain.Transaction;
import com.bank.light.exceptions.ExcelExporterException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Transaction> list;

    private ExcelExporter(List<Transaction> list) {
        this.list = list;
        this.workbook = new XSSFWorkbook();
    }


    private void writeHeaderLine() {
        this.sheet = workbook.createSheet("Transactions");

        Row row = this.sheet.createRow(0);

        CellStyle style = this.workbook.createCellStyle();
        XSSFFont font = this.workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        int columnCount = 0;

        createCell(row, columnCount++, "ID", style);
        createCell(row, columnCount++, "Account", style);
        createCell(row, columnCount++, "Amount", style);
        createCell(row, columnCount++, "Purpose", style);
        createCell(row, columnCount++, "Date", style);
        createCell(row, columnCount++, "Receiver", style);
        createCell(row, columnCount++, "Sender", style);

    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        this.sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = this.workbook.createCellStyle();
        XSSFFont font = this.workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Transaction transaction : this.list) {
            Row row = this.sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, transaction.getId().toString(), style);
            createCell(row, columnCount++, transaction.getAccountUsername(), style);
            createCell(row, columnCount++, transaction.getAmount().toString(), style);
            createCell(row, columnCount++, transaction.getPurpose(), style);
            createCell(row, columnCount++, transaction.getCreatedAtFormatted(), style);
            createCell(row, columnCount++, transaction.getReceiverUsername(), style);
            createCell(row, columnCount++, transaction.getSenderUsername(), style);
        }
    }

    private void writeToOutput(ByteArrayOutputStream outputStream) throws IOException {
        writeHeaderLine();
        writeDataLines();
        this.workbook.write(outputStream);
        this.workbook.close();
    }

    public static ResponseEntity<byte[]> export(List<Transaction> transactions) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelExporter excelExporter = new ExcelExporter(transactions);
        try {
            excelExporter.writeToOutput(out);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "transactions.xlsx");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new ExcelExporterException("Error writing file to output stream. %s".formatted(e.getMessage()));
        }
    }
}