package datapath.procurementdata.documentparser.dao.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Document {
    @JsonProperty("_id")
    private String id;
    private String tenderId;
    private String procuringEntityIdentifier;
    private String tenderDateModified;
    private String url;
    private String type;
    private String title;
    private Map<String, Object> attributes = new HashMap<>();
    private String text;
    private boolean contentInFile;
    private boolean hasTable;
    private boolean parsed;
}
