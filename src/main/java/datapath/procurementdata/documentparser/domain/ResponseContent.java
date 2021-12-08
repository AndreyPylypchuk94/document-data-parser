package datapath.procurementdata.documentparser.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseContent {
    private String format;
    private byte[] content;
}
