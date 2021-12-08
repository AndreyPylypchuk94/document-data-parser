package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.domain.DocumentContent;
import datapath.procurementdata.documentparser.domain.ResponseContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    public DocumentContent parse(ResponseContent responseContent) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(responseContent.getContent());
        DocumentParseable parser = parsers.getOrDefault(responseContent.getFormat().toUpperCase(), parsers.get("DEFAULT"));
        DocumentContent content = parser.parse(inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
