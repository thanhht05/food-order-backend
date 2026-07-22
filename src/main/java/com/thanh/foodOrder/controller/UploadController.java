package com.thanh.foodorder.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thanh.foodorder.service.UploadFileService;
import com.thanh.foodorder.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")

public class UploadController {
    private final UploadFileService uploadFileService;

    public UploadController(UploadFileService uploadFileService) {
        this.uploadFileService = uploadFileService;
    }

    // upload single file
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
    @RequestParam("file") MultipartFile file) {

    String path = uploadFileService.uploadFile(file);

    Map<String, String> response = new HashMap<>();
    response.put("fileName", path);

    return ResponseEntity.ok(response);
    }

    // upload multi files
    // @PostMapping("/upload")
    // @ApiMessage("Upload file")
    // public ResponseEntity<?> uploadFiles(
    //         @RequestParam("files") MultipartFile[] files) {

    //     List<String> paths = new ArrayList<>();

    //     for (MultipartFile file : files) {
    //         String path = uploadFileService.uploadFile(file);
    //         paths.add(path);
    //     }

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("fileNames", paths);

    //     return ResponseEntity.ok(response);
    // }

}
