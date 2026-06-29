package com.quickbite.backend.common.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.common.dto.ImageUploadResponse;
import com.quickbite.backend.common.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Image Upload Controller", description = "Merchant image upload and public image retrieval")
public class ImageUploadController {

    private final ImageStorageService imageStorageService;

    @PostMapping(value = "/merchant/uploads/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload merchant menu or profile image", description = "Stores an image file and returns a URL that can be persisted on restaurant or menu records")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(@RequestPart("file") MultipartFile file) {
        log.info("Merchant image upload requested: {}", file.getOriginalFilename());
        ImageUploadResponse response = imageStorageService.storeImage(file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully.", response));
    }

    @GetMapping("/uploads/images/{fileName:.+}")
    @Operation(summary = "Load uploaded image", description = "Returns an uploaded merchant image for browser rendering")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource resource = imageStorageService.loadImage(fileName);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
