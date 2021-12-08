package datapath.procurementdata.documentparser.service;

import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.nio.file.Path.of;

@Service
public class FileStorageService {

    private static final String FILES_DIRECTORY = "files/";
    private static final String TEXT_FILE_FORMAT = ".txt";
    private static final String TABLE_FILE_FORMAT = ".xlsx";

    @SneakyThrows
    public void write(String text, String id) {
        Path filePath = getFilePath(id, TEXT_FILE_FORMAT);
        deleteIfExists(filePath);
        createFile(filePath);
        writeString(filePath, text);
    }

    @SneakyThrows
    public void write(XSSFWorkbook workbook, String id) {
        Path filePath = getFilePath(id, TABLE_FILE_FORMAT);
        deleteIfExists(filePath);
        createFile(filePath);
        OutputStream outputStream = newOutputStream(filePath);
        workbook.write(outputStream);
        outputStream.close();
    }

    public String read(String id) throws IOException {
        Path filePath = getFilePath(id, TEXT_FILE_FORMAT);
        if (!exists(filePath)) throw new RuntimeException("File not found");
        return readString(filePath);
    }

    private Path getFilePath(String id, String format) {
        return of(FILES_DIRECTORY + id + format);
    }
}
