package datapath.procurementdata.documentparser.service.impl;

import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.service.DocumentParseable;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

import static datapath.procurementdata.documentparser.service.DocumentParsingUtils.toZonedDateTimeString;

@Service
public class DocParser implements DocumentParseable {

    @Override
    public DocumentContent parse(InputStream inputStream) throws IOException {
        HWPFDocument doc = new HWPFDocument(inputStream);
        DocumentContent documentContent = new DocumentContent();
        documentContent.setText(doc.getText().toString());
        putAttributes(documentContent, doc);
        return documentContent;
    }

    private void putAttributes(DocumentContent documentContent, HWPFDocument doc) {
        SummaryInformation summaryInformation = doc.getSummaryInformation();
        DocumentSummaryInformation documentSummaryInformation = doc.getDocumentSummaryInformation();
        documentContent.getAttributes().put("author", summaryInformation.getAuthor());
        documentContent.getAttributes().put("created", toZonedDateTimeString(summaryInformation.getCreateDateTime()));
        documentContent.getAttributes().put("lastModifiedBy", summaryInformation.getLastAuthor());
        documentContent.getAttributes().put("modified", toZonedDateTimeString(summaryInformation.getLastSaveDateTime()));
        documentContent.getAttributes().put("title", summaryInformation.getTitle());
        documentContent.getAttributes().put("pageCount", summaryInformation.getPageCount());
        documentContent.getAttributes().put("subject", summaryInformation.getSubject());
        documentContent.getAttributes().put("company", documentSummaryInformation.getCompany());
        documentContent.getAttributes().put("category", documentSummaryInformation.getCategory());
        documentContent.getAttributes().put("status", documentSummaryInformation.getContentStatus());
        documentContent.getAttributes().put("manager", documentSummaryInformation.getManager());
        documentContent.getAttributes().put("application", summaryInformation.getApplicationName());
        documentContent.getAttributes().put("version", documentSummaryInformation.getApplicationVersion());
        documentContent.getAttributes().put("extension", extension());
    }

    @Override
    public String extension() {
        return "DOC";
    }
}
