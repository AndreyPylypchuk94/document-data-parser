package datapath.procurementdata.documentparser.service.impl;

import datapath.procurementdata.documentparser.dao.DocumentContent;
import datapath.procurementdata.documentparser.exception.NotFoundParserException;
import datapath.procurementdata.documentparser.service.DocumentParseable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
public class DefaultParser implements DocumentParseable {
    @Override
    public DocumentContent parse(InputStream inputStream) {
        throw new NotFoundParserException();
    }

    @Override
    public String extension() {
        return "DEFAULT";
    }
}
