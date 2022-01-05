package datapath.procurementdata.documentparser.dao.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsingResult {
    @JsonProperty("_id")
    private String id;
    private Integer symbolCount;
    private String selectedText;
    private List<RegexResult> regexResult = new ArrayList<>();

    @Data
    public static class RegexResult {
        private String regex;
        private List<PositionResult> positionResult = new ArrayList<>();
    }

    @Data
    public static class PositionResult {
        private Integer startIndex;
        private String selectedText;
    }
}
