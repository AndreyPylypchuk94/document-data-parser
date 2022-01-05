package datapath.procurementdata.documentparser.service.parser;

import datapath.procurementdata.documentparser.domain.DocumentContent;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentParseable {
    DocumentContent parse(InputStream inputStream) throws IOException;

    String extension();
}
