package com.bank.light.exceptions;

public class ExcelExporterException extends RuntimeException {
    public ExcelExporterException(String message) {
        super("Excel exporter exception: " + message);
    }
}
