package datapath.procurementdata.documentparser.dao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import datapath.procurementdata.documentparser.dao.entity.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;

@Service
@RequiredArgsConstructor
public class DocumentDaoService {

    @Value("${result.collection.name}")
    private String COLLECTION_NAME;

    private final MongoTemplate template;
    private final ObjectMapper mapper;

    public void save(Document document) throws JsonProcessingException {
        template.save(mapper.writeValueAsString(document), COLLECTION_NAME);
    }

    public String getLastTenderDateModified() {
        Query query = new Query().with(by(DESC, "tenderDateModified"));
        Document document = template.findOne(query, Document.class, COLLECTION_NAME);
        return isNull(document) || isNull(document.getTenderDateModified()) ?
                null :
                document.getTenderDateModified();
    }
}
