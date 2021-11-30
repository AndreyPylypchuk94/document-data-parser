package datapath.procurementdata.documentparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.dao.Tender;
import datapath.procurementdata.documentparser.domain.FileContent;
import datapath.procurementdata.documentparser.service.DocumentFormatProvider;
import datapath.procurementdata.documentparser.service.DocumentLoader;
import datapath.procurementdata.documentparser.service.DocumentParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@AllArgsConstructor
@SpringBootApplication
public class DocumentParserApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DocumentParserApplication.class, args);
    }

    private final DocumentLoader loadService;
    private final DocumentFormatProvider formatProvider;
    private final DocumentParser documentParser;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper mapper;

    @Override
    public void run(String... args) {


        Query query = new Query(where("procurementMethod").is("open"))
//                .with(Sort.by(DESC, "dateModified"))
                .limit(100);

        List<Tender> tenders = mongoTemplate.find(query, Tender.class, "rawProzorro");

        tenders.forEach(t -> t.getDocuments()
                .stream()
                .filter(d -> "text/plain".equals(d.getFormat()))
                .map(d -> {
                    Response response = loadService.load(d);
                    FileContent fileContent = formatProvider.provide(response);
                    DocumentContent documentContent = documentParser.parse(fileContent);

                    if (isNull(documentContent)) return null;

                    documentContent.setTenderId(t.getTenderID());
                    documentContent.setProcuringEntityIdentifier(t.getProcuringEntity().getIdentifier().getId());
                    documentContent.setId(d.getId());
                    documentContent.setTitle(d.getTitle());
                    documentContent.setType(d.getDocumentType());
                    documentContent.setUrl(d.getUrl());

                    return documentContent;
                })
                .filter(Objects::nonNull)
                .forEach(d -> {
                    try {
                        mongoTemplate.save(mapper.writeValueAsString(d), "documentContent");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }
}
