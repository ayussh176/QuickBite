package com.quickbite.backend.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodImageDto {

    private Long id;
    private String imageUrl;
    private boolean primary;
    private Integer sortOrder;
}
