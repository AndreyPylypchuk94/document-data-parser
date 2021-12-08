package datapath.procurementdata.documentparser.service.impl;

import datapath.procurementdata.documentparser.domain.DocumentContent;
import datapath.procurementdata.documentparser.service.DocumentParseable;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static datapath.procurementdata.documentparser.service.DocumentParsingUtils.toZonedDateTimeString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class DocxParser implements DocumentParseable {

    @Override
    public DocumentContent parse(InputStream inputStream) throws IOException {
        XWPFDocument xdoc = new XWPFDocument(inputStream);
        XWPFWordExtractor ex = new XWPFWordExtractor(xdoc);
        DocumentContent documentContent = new DocumentContent();
        documentContent.setText(ex.getText());
        putAttributes(documentContent, xdoc);
        putTables(documentContent, xdoc);
        return documentContent;
    }

    private void putTables(DocumentContent documentContent, XWPFDocument xdoc) {
        if (xdoc.getTables() == null || xdoc.getTables().size() == 0) return;

        XSSFWorkbook workbook = new XSSFWorkbook();
        List<IBodyElement> elements = xdoc.getBodyElements();
        for (int i = 0; i < elements.size(); i++) {
            IBodyElement element = elements.get(i);
            if (element instanceof XWPFTable table) {
                if (table.getNumberOfRows() <= 1) continue;
                String title = extractTableTitle(elements, i);
                XSSFSheet sheet;
                try {
                    sheet = workbook.createSheet(isNotBlank(title) ? title : "Sheet " + i);
                } catch (Exception e) {
                    sheet = workbook.createSheet();
                }
                handleSheet(sheet, table);
            }
        }
        documentContent.setWorkbook(workbook);
    }

    private void handleSheet(XSSFSheet sheet, XWPFTable table) {
        for (int i = 0; i < table.getNumberOfRows(); i++) {
            handleRow(table, sheet, i);
        }
    }

    private void handleRow(XWPFTable table, XSSFSheet sheet, int rowIdx) {
        XSSFRow row = sheet.createRow(rowIdx);
        XWPFTableRow sourceRow = table.getRow(rowIdx);

        List<XWPFTableCell> cells = sourceRow.getTableCells();
        for (int i = 0; i < cells.size(); i++) {
            handleCell(row, cells, i);
        }
    }

    private void handleCell(XSSFRow row, List<XWPFTableCell> cells, int i) {
        XSSFCell cell = row.createCell(i);
        cell.setCellValue(cells.get(i).getText());
    }

    private String extractTableTitle(List<IBodyElement> elements, int i) {
        String title = null;
        try {
            for (int pi = i - 1; pi >= 0; pi--) {
                IBodyElement element = elements.get(pi);
                if (element instanceof XWPFParagraph p) {
                    title = p.getText();
                    if (isNotBlank(title)) {
                        break;
                    }
                } else break;
            }
        } catch (Exception ignored) {
        }
        return clear(title);
    }

    private String clear(String title) {
        if (title == null) return null;
        return title.replaceAll("[^0-9A-Za-zА-ЩЬЮЯҐЄІЇа-щьюяґєії .,]", "");
    }

    private void putAttributes(DocumentContent documentContent, XWPFDocument xdoc) {
        try {
            PackageProperties props = xdoc.getPackage().getPackageProperties();
            documentContent.getAttributes().put("author", get(props.getCreatorProperty()));
            documentContent.getAttributes().put("created", toZonedDateTimeString(get(props.getCreatedProperty())));
            documentContent.getAttributes().put("lastModifiedBy", get(props.getLastModifiedByProperty()));
            documentContent.getAttributes().put("modified", toZonedDateTimeString(get(props.getModifiedProperty())));
            documentContent.getAttributes().put("title", get(props.getTitleProperty()));
            documentContent.getAttributes().put("pageCount", xdoc.getProperties().getExtendedProperties().getPages());
            documentContent.getAttributes().put("subject", get(props.getSubjectProperty()));
            documentContent.getAttributes().put("company", xdoc.getProperties().getExtendedProperties().getCompany());
            documentContent.getAttributes().put("category", get(props.getCategoryProperty()));
            documentContent.getAttributes().put("status", get(props.getContentStatusProperty()));
            documentContent.getAttributes().put("manager", xdoc.getProperties().getExtendedProperties().getManager());
            documentContent.getAttributes().put("application", xdoc.getProperties().getExtendedProperties().getApplication());
            documentContent.getAttributes().put("version", xdoc.getProperties().getExtendedProperties().getAppVersion());
            documentContent.getAttributes().put("extension", extension());
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T get(Optional<T> value) {
        return value.orElse(null);
    }

    @Override
    public String extension() {
        return "DOCX";
    }
}
