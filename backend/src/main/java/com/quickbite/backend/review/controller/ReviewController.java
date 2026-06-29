package com.quickbite.backend.review.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.review.dto.ReviewReplyRequest;
import com.quickbite.backend.review.dto.ReviewRequest;
import com.quickbite.backend.review.dto.ReviewResponse;
import com.quickbite.backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "Endpoints for creating and reading customer reviews and merchant replies")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit a review", description = "Allows customers to rate and review delivered orders. Re-calculates restaurant average ratings.")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody ReviewRequest request,
                                                                    Principal principal) {
        log.info("Review submission requested by: {}", principal.getName());
        ReviewResponse response = reviewService.createReview(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully.", response));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get restaurant reviews", description = "Retrieves a paginated list of all reviews and replies for a specific restaurant")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getRestaurantReviews(@PathVariable Long restaurantId,
                                                                                  Pageable pageable) {
        log.info("Fetching reviews for restaurant ID: {}", restaurantId);
        Page<ReviewResponse> response = reviewService.getRestaurantReviews(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched successfully.", response));
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get customer reviews", description = "Fetches a paginated history of all reviews written by the logged-in customer")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(Pageable pageable, Principal principal) {
        log.info("Fetching reviews written by customer: {}", principal.getName());
        Page<ReviewResponse> response = reviewService.getCustomerReviews(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Your reviews fetched successfully.", response));
    }

    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reply to a review", description = "Allows restaurant owners to respond to a customer review left on one of their orders")
    public ResponseEntity<ApiResponse<ReviewResponse>> replyToReview(@PathVariable Long reviewId,
                                                                     @Valid @RequestBody ReviewReplyRequest request,
                                                                     Principal principal) {
        log.info("Reply to review ID {} requested by: {}", reviewId, principal.getName());
        ReviewResponse response = reviewService.replyToReview(principal.getName(), reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Reply added successfully.", response));
    }
}
