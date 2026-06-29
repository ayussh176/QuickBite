package com.quickbite.backend.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyRequest {

    @NotBlank(message = "Reply text is required")
    @Size(max = 2000, message = "Reply must not exceed 2000 characters")
    private String reply;
}
