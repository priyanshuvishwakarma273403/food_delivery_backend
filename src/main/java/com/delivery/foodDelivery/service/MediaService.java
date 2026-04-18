package com.delivery.foodDelivery.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface MediaService {
    String uploadImage(MultipartFile file) throws IOException;
    void deleteImage(String imageUrl) throws IOException;
}
