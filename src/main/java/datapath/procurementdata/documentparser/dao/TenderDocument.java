package datapath.procurementdata.documentparser.dao;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class TenderDocument {
    @Field(name = "id")
    private String id;
    private String format;
    private String url;
    private String documentType;
    private String title;
}
