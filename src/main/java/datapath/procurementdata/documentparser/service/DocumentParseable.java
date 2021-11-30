package datapath.procurementdata.documentparser.service;

import datapath.procurementdata.documentparser.dao.DocumentContent;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentParseable {
    DocumentContent parse(InputStream inputStream) throws IOException;

    String extension();
}
