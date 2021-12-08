package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.domain.ResponseContent;
import org.jsoup.Connection.Response;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

@Service
public class ResponseParser {

    public ResponseContent parse(Response response) {
        byte[] content = response.bodyAsBytes();
        return new ResponseContent(extractFormat(response), content);
    }

    private String extractFormat(Response response) {
        String type = response.header("content-disposition");
        return substringAfterLast(type, ".");
    }
}
