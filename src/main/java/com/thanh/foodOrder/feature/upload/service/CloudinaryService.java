package com.thanh.foodorder.feature.upload.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.thanh.foodorder.feature.upload.DTO.ImgResponseDTO;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;

    }

    public ImgResponseDTO uploadImage(MultipartFile file, String target) {

        try {

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", target));

            ImgResponseDTO res = new ImgResponseDTO();
            res.setImgUrl(result.get("secure_url").toString());
            return res;
        } catch (IOException e) {
            throw new RuntimeException("Upload image failed", e);
        }

    }
}
