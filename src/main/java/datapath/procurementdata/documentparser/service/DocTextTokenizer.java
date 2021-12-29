package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.dao.entity.Document;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.util.CollectionUtils.isEmpty;


@Slf4j
@Service
@AllArgsConstructor
public class DocTextTokenizer {

    private final MongoTemplate template;

//    @Scheduled(fixedDelay = 1000)
    public void run() {
        Map<String, Long> result = new HashMap<>();

        List<Document> documents;
        int page = 0;

        do {
            log.info("Processing page {}", page);

            documents = template.find(
                    new Query(where("text").ne(null)).with(of(page, 100)), Document.class, "cpvGroupDocuments");

            if (isEmpty(documents)) break;

            documents.stream()
                    .map(Document::getText)
                    .filter(StringUtils::isNotBlank)
                    .flatMap(t -> Arrays.stream(t.split("\r\r")))
                    .flatMap(t -> Arrays.stream(t.split("\r \r")))
                    .flatMap(t -> Arrays.stream(t.split("\n\n")))
                    .flatMap(t -> Arrays.stream(t.split("\n \n")))
                    .filter(StringUtils::isNotBlank)
                    .map(p -> StringUtils.substringBefore(p, "."))
                    .map(p -> StringUtils.substringBefore(p, ","))
                    .map(p -> p.replaceAll("\n", " "))
                    .map(p -> p.replaceAll("\t", " "))
                    .map(p -> p.replaceAll("\r", " "))
                    .map(p -> p.replaceAll("\f", " "))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .forEach(t -> add(result, t));

            page++;
        } while (!isEmpty(documents));

        result.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    try {
                        Files.writeString(Path.of("result.tsv"), e.getKey() + "\t" + e.getValue() + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
        log.info("finished");
    }

    private void add(Map<String, Long> result, String text) {
        result.computeIfPresent(text, (k, v) -> v + 1);
        result.putIfAbsent(text, 1L);
    }
}
