package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.dao.entity.TenderDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

@Slf4j
@Service
public class DocumentFilterService {

    private static final List<String> AVAILABLE_EXTENSIONS = List.of("DOC", "DOCX", "PDF");

    public boolean isValid(TenderDocument d) {
        try {
            if (!"text/plain".equals(d.getFormat())) return false;
            String extension = substringAfterLast(d.getTitle(), ".");
            extension = extension.toUpperCase();
            return AVAILABLE_EXTENSIONS.contains(extension);
        } catch (Exception e) {
            log.error("Document check validation error", e);
            return false;
        }
    }
}
