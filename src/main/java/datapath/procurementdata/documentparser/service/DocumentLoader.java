package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.dao.TenderDocument;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class DocumentLoader {

    public Response load(TenderDocument tenderDocument) {
        Response response = get(tenderDocument.getUrl());
        String fileLocation = response.header("Location");
        return get(fileLocation);
    }

    private Response get(String url) {
        log.info("Fetching {}", url);
        try {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .followRedirects(false)
                    .maxBodySize(0)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed loading " + url, e);
        }
    }
}
