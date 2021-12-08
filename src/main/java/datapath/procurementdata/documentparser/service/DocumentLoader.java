package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.dao.entity.Tender;
import datapath.procurementdata.documentparser.dao.service.DocumentDaoService;
import datapath.procurementdata.documentparser.dao.service.TenderDaoService;
import datapath.procurementdata.documentparser.domain.DocumentContent;
import datapath.procurementdata.documentparser.domain.ResponseContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLoader {

    @Value("${document.loading.enable}")
    private boolean enable;

    private final ProzorroApiService prozorroApiService;
    private final ResponseParser responseParser;
    private final DocumentParser documentParser;
    private final DocumentFilterService filterService;
    private final DocumentHandler handler;
    private final DocumentDaoService documentDaoService;
    private final TenderDaoService tenderDaoService;

    @Scheduled(fixedDelay = 1000 * 60)
    private void load() {
        if (!enable) return;

        log.info("Started");

        String lastTenderDocDate = documentDaoService.getLastTenderDateModified();

        int size = 10;
        int page = 0;
        List<Tender> tenders;
        do {
            log.info("Processing {} page after {} date", page, lastTenderDocDate);

            tenders = tenderDaoService.getPageAfterDate(page, size, lastTenderDocDate);

            if (isEmpty(tenders)) break;

            tenders.forEach(t -> t.getDocuments()
                    .stream()
                    .filter(filterService::isValid)
                    .forEach(d -> {
                        try {
                            Connection.Response response = prozorroApiService.load(d);
                            ResponseContent responseContent = responseParser.parse(response);
                            DocumentContent documentContent = documentParser.parse(responseContent);
                            if (documentContent.getText().getBytes().length <= responseContent.getContent().length)
                                handler.handle(t, d, documentContent);
                            else
                                throw new RuntimeException("Invalid parsed content length");
                        } catch (Exception e) {
                            log.error("error", e);
                        }
                    })
            );

            page++;
        } while (!isEmpty(tenders));

        log.info("Finished");
    }
}
