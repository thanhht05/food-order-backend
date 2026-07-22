package com.thanh.foodorder.service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thanh.foodorder.util.exception.CommonException;

@Service
public class UploadFileService {

    @Value("${thanh.upload-file.base-uri}")
    private String baseUri;

    public String uploadFile(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new CommonException("File is empty");
            }

            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                throw new CommonException("Only image files are allowed");
            }

            // original filename
            String originalName = file.getOriginalFilename();

            if (originalName == null || !originalName.contains(".")) {
                throw new CommonException("Invalid file name");
            }

            // extension (.jpg .png ...)
            String ext = originalName.substring(originalName.lastIndexOf("."));

            // new name with UUID
            String fileName = UUID.randomUUID() + ext;

            // root folder (D:/upload)
            Path root = Paths.get(URI.create(baseUri));

            // create root folder if missing
            Files.createDirectories(root);

            // full file path
            Path filePath = root.resolve(fileName);

            // save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // return filename only (store in DB)
            return fileName;

        } catch (Exception e) {
            throw new CommonException("Failed to store file: " + e.getMessage());
        }
    }
}