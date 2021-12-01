package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.domain.FileContent;
import datapath.procurementdata.documentparser.exception.NotFoundParserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class DocumentParser {

    private final Map<String, DocumentParseable> parsers;

    public DocumentParser(List<DocumentParseable> parsers) {
        this.parsers = parsers.stream()
                .collect(toMap(DocumentParseable::extension, identity()));
    }

    public DocumentContent parse(FileContent fileContent) {
        try {
            DocumentParseable parser = parsers.getOrDefault(fileContent.getFormat().toUpperCase(), parsers.get("DEFAULT"));
            DocumentContent documentContent = parser.parse(fileContent.getContent());
            fileContent.getContent().close();
            return documentContent;
        } catch (NotFoundParserException e) {
            log.warn("Unknown format {}", fileContent.getFormat());
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
