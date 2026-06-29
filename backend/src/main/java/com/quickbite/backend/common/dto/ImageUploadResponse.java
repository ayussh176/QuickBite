package com.quickbite.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {

    private String fileName;
    private String imageUrl;
    private String contentType;
    private long size;
}
