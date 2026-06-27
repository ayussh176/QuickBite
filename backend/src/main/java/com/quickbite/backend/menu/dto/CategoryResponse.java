package com.quickbite.backend.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer sortOrder;
    private boolean active;
}
