package datapath.procurementdata.documentparser.dao;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Party {
    private String name;
    private Identifier identifier;

    @Data
    public static class Identifier {
        @Field(name = "id")
        private String id;
        private String legalName;
    }
}
