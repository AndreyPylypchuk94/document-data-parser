package datapath.procurementdata.documentparser.service.impl;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.service.DocumentParseable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

import static com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage;

@Service
public class PdfParser implements DocumentParseable {

    private final static SimpleTextExtractionStrategy strategy = new SimpleTextExtractionStrategy();

    @Override
    public DocumentContent parse(InputStream inputStream) throws IOException {
        PdfReader reader = new PdfReader(inputStream);
        DocumentContent documentContent = new DocumentContent();
        documentContent.setText(extractText(reader));
        putAttributes(documentContent, reader);
        return documentContent;
    }

    private String extractText(PdfReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            builder.append(getTextFromPage(reader, i, strategy));
        }
        return builder.toString();
    }

    private void putAttributes(DocumentContent documentContent, PdfReader reader) {
        documentContent.getAttributes().putAll(reader.getInfo());
    }

    @Override
    public String extension() {
        return "PDF";
    }
}
