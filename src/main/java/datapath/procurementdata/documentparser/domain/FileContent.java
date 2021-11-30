package datapath.procurementdata.documentparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class FileContent {
    private String format;
    private InputStream content;
}
