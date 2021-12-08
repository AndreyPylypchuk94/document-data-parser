package datapath.procurementdata.documentparser.domain;

import lombok.Data;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Map;

@Data
public class DocumentContent {
    private Map<String, Object> attributes = new HashMap<>();
    private String text;
    private XSSFWorkbook workbook;
}
