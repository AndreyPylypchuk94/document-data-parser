package datapath.procurementdata.documentparser.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DocumentContent {
    @JsonProperty("_id")
    private String id;
    private String tenderId;
    private String procuringEntityIdentifier;
    private String url;
    private String type;
    private String title;
    private Map<String, Object> attributes = new HashMap<>();
    private String text;
    private boolean contentInFile;
    private boolean unprocessedFile;
}
