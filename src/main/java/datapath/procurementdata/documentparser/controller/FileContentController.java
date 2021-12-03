package datapath.procurementdata.documentparser.controller;

import datapath.procurementdata.documentparser.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("file-contents")
public class FileContentController {

    private final FileStorageService service;

    @GetMapping("{id}")
    public ResponseEntity<Map<String, String>> get(@PathVariable String id) {
        try {
            return new ResponseEntity<>(singletonMap("content", service.read(id)), OK);
        } catch (Exception e) {
            return new ResponseEntity<>(singletonMap("error", e.getMessage()), NOT_FOUND);
        }
    }
}
