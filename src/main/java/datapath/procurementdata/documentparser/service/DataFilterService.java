package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.dao.entity.Classification;
import datapath.procurementdata.documentparser.dao.entity.Item;
import datapath.procurementdata.documentparser.dao.entity.Tender;
import datapath.procurementdata.documentparser.dao.entity.TenderDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
public class DataFilterService {

    @Value("${processed.cpv.groups}")
    private List<String> PROCESSED_CPV_GROUPS;

    private static final List<String> AVAILABLE_EXTENSIONS = List.of("DOC", "DOCX", "PDF");

    public boolean isProcessed(TenderDocument d) {
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

    public boolean isProcessed(Tender tender) {
        if (isEmpty(PROCESSED_CPV_GROUPS)) return true;
        return tender.getItems()
                .stream()
                .map(Item::getClassification)
                .filter(Objects::nonNull)
                .map(Classification::getId)
                .filter(Objects::nonNull)
                .anyMatch(id -> {
                    String cpvGroup = id.substring(0, 2);
                    return PROCESSED_CPV_GROUPS.contains(cpvGroup);
                });
    }
}
