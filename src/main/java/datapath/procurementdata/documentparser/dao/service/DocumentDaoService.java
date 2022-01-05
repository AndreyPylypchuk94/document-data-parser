package datapath.procurementdata.documentparser.dao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import datapath.procurementdata.documentparser.dao.entity.Document;
import datapath.procurementdata.documentparser.dao.entity.ParsingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
public class DocumentDaoService {

    @Value("${result.collection.name}")
    private String COLLECTION_NAME;
    @Value("${result.parsing.collection.name}")
    private String PARSING_RESULT_COLLECTION_NAME;

    private static final Query GET_NOT_PARSED_QUERY = new Query(
            new Criteria().andOperator(
                    new Criteria().orOperator(
                            where("parsed").exists(false),
                            where("parsed").is(false)
                    ),
                    new Criteria().andOperator(
                            where("text").exists(true),
                            where("text").ne(null)
                    )
            )


    ).limit(10);

    private final MongoTemplate template;
    private final ObjectMapper mapper;

    public void save(Document document) throws JsonProcessingException {
        template.save(mapper.writeValueAsString(document), COLLECTION_NAME);
    }

    public void save(ParsingResult parsingResult) throws JsonProcessingException {
        template.save(mapper.writeValueAsString(parsingResult), PARSING_RESULT_COLLECTION_NAME);
    }

    public String getLastTenderDateModified() {
        Query query = new Query().with(by(DESC, "tenderDateModified"));
        Document document = template.findOne(query, Document.class, COLLECTION_NAME);
        return isNull(document) || isNull(document.getTenderDateModified()) ?
                null :
                document.getTenderDateModified();
    }

    public List<Document> getNotParsed() {
        return template.find(GET_NOT_PARSED_QUERY, Document.class, COLLECTION_NAME);
    }
}
