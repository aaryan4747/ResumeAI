package com.aaryan.resumeai.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

@Service
public class DocumentService {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            return tika.parseToString(stream);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract text from " + file.getOriginalFilename() + ": " + e.getMessage());
        }
    }
}
