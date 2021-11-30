package datapath.procurementdata.documentparser.dao;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "rawProzorro")
public class Tender {
    private String id;
    private String tenderID;
    private Party procuringEntity;
    private List<TenderDocument> documents = new ArrayList<>();
}
