package com.thanh.foodorder.feature.upload.controller;

import java.io.IOException;
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
import com.thanh.foodorder.core.util.annotation.ApiMessage;
import com.thanh.foodorder.feature.upload.DTO.ImgResponseDTO;
import com.thanh.foodorder.feature.upload.service.CloudinaryService;
import com.thanh.foodorder.feature.upload.service.UploadFileService;

@RestController
@RequestMapping("/api/v1/cloudinary")

public class UploadController {
    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ImgResponseDTO> uploadImage(
            @RequestParam("file") MultipartFile file) {

        ImgResponseDTO imageUrl = cloudinaryService.uploadImage(file, "food-order/products");

        return ResponseEntity.ok(imageUrl);
    }

}
