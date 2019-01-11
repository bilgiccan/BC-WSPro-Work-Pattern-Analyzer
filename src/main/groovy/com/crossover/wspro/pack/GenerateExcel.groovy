package com.crossover.wspro.pack

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
class GenerateExcel {
    XSSFWorkbook workbook

    private void createDataRow(sheet, aBook, row) {
        def dataRow = sheet.createRow(row)
        int columnCount = 0
        aBook.each { key, value ->
            Cell cell = dataRow.createCell(++columnCount)
            if (value instanceof String) {
                cell.setCellValue(value)
            } else if (value instanceof Number) {
                cell.setCellValue(value.intValue())
            } else {
                cell.setCellValue(value.toString())
            }
        }
    }

    private void createHeader(sheet, bookData) {
        int columnCount = 0
        def headerRow = sheet.createRow(0)
        bookData.get(0).keySet().each({ key ->
            headerRow.createCell(++columnCount).cellValue = key
        })
    }

    private XSSFSheet getSheetByName(String sheetName) {
        def sheet = workbook.getSheet(sheetName)
        sheet ? sheet : workbook.createSheet(sheetName)
    }

    @PostConstruct
    void init() {
        workbook = new XSSFWorkbook()
    }

    void print(bookData, String sheetName) {
        if (bookData) {
            def row = 0
            def sheet = getSheetByName(sheetName)
            createHeader(sheet, bookData)
            bookData.each {
                createDataRow(sheet, it, ++row)
            }
        }
    }

    void writeData() {
        try {
            workbook.write(new FileOutputStream("Gemba-Walk.xlsx"))
        } catch (e) {
            e.printStackTrace()
        }
    }

}