package datapath.procurementdata.documentparser.dao.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Classification {
    @Field("id")
    private String id;
}
