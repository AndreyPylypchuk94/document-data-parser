package datapath.procurementdata.documentparser.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.nio.file.Path.of;

@Service
public class FileStorageService {

    private static final String FILES_DIRECTORY = "files/";
    private static final String FILE_FORMAT = ".txt";

    @SneakyThrows
    public void write(String text, String id) {
        Path filePath = getFilePath(id);
        deleteIfExists(filePath);
        createFile(filePath);
        writeString(filePath, text);
    }

    public String read(String id) throws IOException {
        Path filePath = getFilePath(id);
        if (!exists(filePath)) throw new RuntimeException("File not found");
        return readString(filePath);
    }

    private Path getFilePath(String id) {
        return of(FILES_DIRECTORY + id + FILE_FORMAT);
    }
}
