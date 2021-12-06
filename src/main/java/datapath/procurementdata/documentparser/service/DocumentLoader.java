package datapath.procurementdata.documentparser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.dao.Tender;
import datapath.procurementdata.documentparser.domain.FileContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLoader {

    private static final String FINAL_COLLECTION_NAME = "documents";
    private static final String RAW_DATE_COLLECTION_NAME = "rawProzorro";
    @Value("${document.loading.enable}")
    private boolean enable;

    private final ConnectionService connectionService;
    private final DocumentFormatProvider formatProvider;
    private final DocumentParser documentParser;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper mapper;
    private final FileStorageService fileStorageService;
    private final DocumentFilterService filterService;

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    private void load() throws IOException {
        if (!enable) return;

        String lastTenderDocDate = getLast();

        log.info("Started");

        Map<String, String> unknownExtensions = new HashMap<>();

        Query query = new Query(where("procurementMethod").is("open")
                .andOperator(where("status").is("complete"))
        );

        if (nonNull(lastTenderDocDate))
            query.addCriteria(where("dateModified").gt(lastTenderDocDate));

        try {
            int size = 10;
            int page = 0;
            List<Tender> tenders;
            do {
                log.info("Processing {} page {}-{} tenders", page, page * size, page * size + size);

                tenders = mongoTemplate.find(query.with(of(page, size, ASC, "dateModified")), Tender.class, RAW_DATE_COLLECTION_NAME);

                if (isEmpty(tenders)) break;

                List<DocumentContent> documents = new ArrayList<>();

                tenders.forEach(t -> t.getDocuments()
                        .stream()
                        .filter(filterService::isValid)
                        .map(d -> {
                            Connection.Response response = connectionService.load(d);
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
                            documentContent.setTenderDateModified(t.getDateModified());

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
                        mongoTemplate.save(doc, FINAL_COLLECTION_NAME);
                    } catch (Exception e) {
                        if (isBlank(document.getText()))
                            throw new RuntimeException(e);

                        fileStorageService.write(document.getText(), document.getId());
                        document.setText(null);
                        document.setContentInFile(true);
                        mongoTemplate.save(mapper.writeValueAsString(document), FINAL_COLLECTION_NAME);
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

    private String getLast() {
        Query query = new Query().with(by(DESC, "tenderDateModified"));
        DocumentContent document = mongoTemplate.findOne(query, DocumentContent.class, FINAL_COLLECTION_NAME);
        return isNull(document) || isNull(document.getTenderDateModified()) ?
                null :
                document.getTenderDateModified();
    }
}
