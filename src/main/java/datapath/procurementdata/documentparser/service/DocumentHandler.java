package datapath.procurementdata.documentparser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import datapath.procurementdata.documentparser.dao.entity.Document;
import datapath.procurementdata.documentparser.dao.entity.Tender;
import datapath.procurementdata.documentparser.dao.entity.TenderDocument;
import datapath.procurementdata.documentparser.dao.service.DocumentDaoService;
import datapath.procurementdata.documentparser.domain.DocumentContent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class DocumentHandler {

    private static final int MAX_TEXT_LENGTH = 10_000_000;

    private final FileStorageService fileStorageService;
    private final DocumentDaoService documentDaoService;

    public void handle(Tender t, TenderDocument d, DocumentContent documentContent) throws JsonProcessingException {
        Document document = new Document();
        document.setId(d.getId());
        document.setTenderId(t.getTenderID());
        document.setProcuringEntityIdentifier(t.getProcuringEntity().getIdentifier().getId());
        document.setTitle(d.getTitle());
        document.setType(d.getDocumentType());
        document.setUrl(d.getUrl());
        document.setTenderDateModified(t.getDateModified());
        document.setAttributes(documentContent.getAttributes());

        if (nonNull(documentContent.getWorkbook())) {
            document.setHasTable(true);
            fileStorageService.write(documentContent.getWorkbook(), d.getId());
        }

        if (documentContent.getText().getBytes().length < MAX_TEXT_LENGTH) {
            document.setText(documentContent.getText());
        } else {
            document.setContentInFile(true);
            fileStorageService.write(documentContent.getText(), d.getId());
        }

        documentDaoService.save(document);
    }
}
