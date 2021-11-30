package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.domain.FileContent;
import org.jsoup.Connection.Response;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

@Service
public class DocumentFormatProvider {

    public FileContent provide(Response response) {
        return new FileContent(extractFormat(response), response.bodyStream());
    }

    private String extractFormat(Response response) {
        String type = response.header("content-disposition");
        return substringAfterLast(type, ".");
    }
}
