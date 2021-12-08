package datapath.procurementdata.documentparser.dao.service;

import datapath.procurementdata.documentparser.dao.entity.Tender;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@AllArgsConstructor
public class TenderDaoService {

    private static final String RAW_DATE_COLLECTION_NAME = "rawProzorro";

    private final MongoTemplate template;

    public List<Tender> getPageAfterDate(int page, int size, String date) {
        Query query = new Query(where("procurementMethod").is("open")
                .andOperator(where("status").is("complete")))
                .with(of(page, size, ASC, "dateModified"));


        if (nonNull(date))
            query.addCriteria(where("dateModified").gt(date));

        return template.find(query, Tender.class, RAW_DATE_COLLECTION_NAME);
    }
}
