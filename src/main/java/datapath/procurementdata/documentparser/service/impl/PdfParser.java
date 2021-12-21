package datapath.procurementdata.documentparser.service.impl;

import com.itextpdf.text.pdf.PdfReader;
import datapath.procurementdata.documentparser.domain.DocumentContent;
import datapath.procurementdata.documentparser.service.DocumentParseable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage;
import static java.time.ZoneOffset.ofHours;
import static java.time.ZonedDateTime.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.*;

@Service
public class PdfParser implements DocumentParseable {

    private final static int MAX_CONTENT_SIZE = 30_000_000;
//    private final static SimpleTextExtractionStrategy STRATEGY = new SimpleTextExtractionStrategy();
    private final static DateTimeFormatter FORMATTER = ofPattern("yyyyMMddHHmmss");

    @Override
    public DocumentContent parse(InputStream inputStream) throws IOException {
        PdfReader reader = new PdfReader(inputStream);
        DocumentContent documentContent = new DocumentContent();
        documentContent.setText(extractText(reader));
        putAttributes(documentContent, reader);
        reader.close();
        return documentContent;
    }

    private String extractText(PdfReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            if (builder.toString().getBytes().length > MAX_CONTENT_SIZE)
                return null;
            builder.append(getTextFromPage(reader, i));
        }
        return builder.toString();
    }

    private void putAttributes(DocumentContent documentContent, PdfReader reader) {
        HashMap<String, String> info = reader.getInfo();
        documentContent.getAttributes().put("author", info.get("Author"));
        documentContent.getAttributes().put("created", convertDate(info.get("CreationDate")));
        documentContent.getAttributes().put("modified", convertDate(info.get("ModDate")));
        documentContent.getAttributes().put("title", info.get("Title"));
        documentContent.getAttributes().put("pageCount", reader.getNumberOfPages());
        documentContent.getAttributes().put("subject", info.get("Subject"));
        documentContent.getAttributes().put("application", info.get("Creator"));
        documentContent.getAttributes().put("version", reader.getPdfVersion());
        documentContent.getAttributes().put("extension", extension());
    }

    private String convertDate(String date) {
        if (isBlank(date)) return null;

        String dateTime = substringBefore(date, "+");
        dateTime = substringBefore(dateTime, "-");
        dateTime = substringBefore(dateTime, "Z");

        if (dateTime.startsWith("D:")) {
            dateTime = substringAfter(dateTime, ":");
        }

        LocalDateTime parsedLocalDateTime = LocalDateTime.parse(dateTime, FORMATTER);

        int zoneHours;
        if (date.contains("+")) {
            String zone = substringBetween(date, "+", "'");
            zoneHours = Integer.parseInt(zone);
        } else if (date.contains("-")) {
            String zone = substringBetween(date, "-", "'");
            zoneHours = Integer.parseInt(zone) * -1;
        } else
            zoneHours = 0;

        ZonedDateTime zonedDateTime = of(parsedLocalDateTime, ofHours(zoneHours));
        return zonedDateTime.toString();
    }

    @Override
    public String extension() {
        return "PDF";
    }
}
