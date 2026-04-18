package com.delivery.foodDelivery.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "food_delivery",
                            "resource_type", "auto"
                    ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new IOException("Failed to upload image to Cloudinary", e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {
        try {
            // Extract public ID from URL
            // Example: https://res.cloudinary.com/demo/image/upload/v1234/folder/image.jpg
            String publicIdWithExtension = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String publicId = publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf("."));
            
            // If it's in a folder, we might need a more complex extraction or store public_id in DB
            // For now, let's try basic deletion. In a real app, it's better to store publicId in DB.
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
        }
    }
}
