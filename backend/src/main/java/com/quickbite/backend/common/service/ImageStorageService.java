package com.quickbite.backend.common.service;

import com.quickbite.backend.common.dto.ImageUploadResponse;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path imageDirectory;

    public ImageStorageService(@Value("${app.upload.image-dir:uploads/images}") String imageDir) {
        this.imageDirectory = Paths.get(imageDir).toAbsolutePath().normalize();
    }

    public ImageUploadResponse storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Only JPG, PNG, and WebP image uploads are supported.");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = FilenameUtils.getExtension(originalName).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported image file extension.");
        }

        try {
            Files.createDirectories(imageDirectory);
            String fileName = UUID.randomUUID() + "." + extension;
            Path target = imageDirectory.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/v1/uploads/images/")
                    .path(fileName)
                    .toUriString();

            log.info("Stored merchant image at {}", target);
            return ImageUploadResponse.builder()
                    .fileName(fileName)
                    .imageUrl(imageUrl)
                    .contentType(contentType)
                    .size(file.getSize())
                    .build();
        } catch (IOException ex) {
            log.error("Unable to store image upload", ex);
            throw new BadRequestException("Unable to store uploaded image.");
        }
    }

    public Resource loadImage(String fileName) {
        try {
            Path imagePath = imageDirectory.resolve(fileName).normalize();
            if (!imagePath.startsWith(imageDirectory)) {
                throw new BadRequestException("Invalid image path.");
            }

            Resource resource = new UrlResource(imagePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Image", "fileName", fileName);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BadRequestException("Invalid image path.");
        }
    }
}
