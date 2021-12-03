package datapath.procurementdata.documentparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.dao.Tender;
import datapath.procurementdata.documentparser.domain.FileContent;
import datapath.procurementdata.documentparser.service.DocumentFormatProvider;
import datapath.procurementdata.documentparser.service.DocumentLoader;
import datapath.procurementdata.documentparser.service.DocumentParser;
import datapath.procurementdata.documentparser.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class DocumentParserApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DocumentParserApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }

    private static final String COLLECTION_NAME = "documents";
    @Value("${document.loading.enable}")
    private boolean enable;

    private final DocumentLoader loadService;
    private final DocumentFormatProvider formatProvider;
    private final DocumentParser documentParser;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper mapper;
    private final FileStorageService fileStorageService;

    @Override
    public void run(String... args) throws IOException {
        if (!enable) return;

        log.info("Started");

        Map<String, String> unknownExtensions = new HashMap<>();

        Query query = new Query(where("procurementMethod").is("open")
                .andOperator(where("status").is("complete"))
        );

        try {
            int size = 10;
            int page = 0;
            List<Tender> tenders;
            do {
                log.info("Processing {} page {}-{} tenders", page, page * size, page * size + size);

                tenders = mongoTemplate.find(query.with(of(page, size)), Tender.class, "rawProzorro");

                if (isEmpty(tenders)) break;

                List<DocumentContent> documents = new ArrayList<>();

                tenders.forEach(t -> t.getDocuments()
                        .stream()
                        .filter(d -> "text/plain".equals(d.getFormat()))
                        .map(d -> {
                            Response response = loadService.load(d);
                            FileContent fileContent = formatProvider.provide(response);
                            DocumentContent documentContent = documentParser.parse(fileContent);

                            if (isNull(documentContent)) {
                                unknownExtensions.putIfAbsent(fileContent.getFormat(), d.getUrl());
                                return null;
                            }

                            documentContent.setTenderId(t.getTenderID());
                            documentContent.setProcuringEntityIdentifier(t.getProcuringEntity().getIdentifier().getId());
                            documentContent.setId(d.getId());
                            documentContent.setTitle(d.getTitle());
                            documentContent.setType(d.getDocumentType());
                            documentContent.setUrl(d.getUrl());

                            if (documentContent.getText().getBytes().length > fileContent.getContent().length) {
                                documentContent.setUnprocessedFile(true);
                                documentContent.setText(null);
                            }

                            return documentContent;
                        })
                        .filter(Objects::nonNull)
                        .forEach(documents::add)
                );

                for (DocumentContent document : documents) {
                    String doc = mapper.writeValueAsString(document);
                    try {
                        mongoTemplate.save(doc, COLLECTION_NAME);
                    } catch (Exception e) {
                        if (isBlank(document.getText()))
                            throw new RuntimeException(e);

                        fileStorageService.write(document.getText(), document.getId());
                        document.setText(null);
                        document.setContentInFile(true);
                        mongoTemplate.save(mapper.writeValueAsString(document), COLLECTION_NAME);
                    }
                }

                page++;
            } while (!isEmpty(tenders));
        } catch (Exception e) {
            log.error("error", e);
        }

        mapper.writeValue(new File(LocalDateTime.now() + ".json"), unknownExtensions);

        log.info("Finished");
    }
}
