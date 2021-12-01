package datapath.procurementdata.documentparser.service.impl;

import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.service.DocumentParseable;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static datapath.procurementdata.documentparser.service.DocumentParsingUtils.toZonedDateTimeString;

@Service
public class DocxParser implements DocumentParseable {

    @Override
    public DocumentContent parse(InputStream inputStream) throws IOException {
        XWPFDocument xdoc = new XWPFDocument(inputStream);
        XWPFWordExtractor ex = new XWPFWordExtractor(xdoc);
        DocumentContent documentContent = new DocumentContent();
        documentContent.setText(ex.getText());
        putAttributes(documentContent, xdoc);
        return documentContent;
    }

    private void putAttributes(DocumentContent documentContent, XWPFDocument xdoc) {
        try {
            PackageProperties props = xdoc.getPackage().getPackageProperties();
            documentContent.getAttributes().put("author", get(props.getCreatorProperty()));
            documentContent.getAttributes().put("created", toZonedDateTimeString(get(props.getCreatedProperty())));
            documentContent.getAttributes().put("lastModifiedBy", get(props.getLastModifiedByProperty()));
            documentContent.getAttributes().put("modified", toZonedDateTimeString(get(props.getModifiedProperty())));
            documentContent.getAttributes().put("title", get(props.getTitleProperty()));
            documentContent.getAttributes().put("pageCount", xdoc.getProperties().getExtendedProperties().getPages());
            documentContent.getAttributes().put("subject", get(props.getSubjectProperty()));
            documentContent.getAttributes().put("company", xdoc.getProperties().getExtendedProperties().getCompany());
            documentContent.getAttributes().put("category", get(props.getCategoryProperty()));
            documentContent.getAttributes().put("status", get(props.getContentStatusProperty()));
            documentContent.getAttributes().put("manager", xdoc.getProperties().getExtendedProperties().getManager());
            documentContent.getAttributes().put("application", xdoc.getProperties().getExtendedProperties().getApplication());
            documentContent.getAttributes().put("version", xdoc.getProperties().getExtendedProperties().getAppVersion());
            documentContent.getAttributes().put("extension", extension());
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T get(Optional<T> value) {
        return value.orElse(null);
    }

    @Override
    public String extension() {
        return "DOCX";
    }
}
