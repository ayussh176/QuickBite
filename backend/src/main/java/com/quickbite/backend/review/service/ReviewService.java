package com.quickbite.backend.review.service;

import com.quickbite.backend.common.enums.OrderStatus;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.repository.OrderRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import com.quickbite.backend.review.dto.ReviewReplyRequest;
import com.quickbite.backend.review.dto.ReviewRequest;
import com.quickbite.backend.review.dto.ReviewResponse;
import com.quickbite.backend.review.entity.Review;
import com.quickbite.backend.review.mapper.ReviewMapper;
import com.quickbite.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(String email, ReviewRequest request) {
        log.info("Creating review for order ID: {} by: {}", request.getOrderId(), email);

        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        // Check ownership
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You cannot review an order placed by someone else.");
        }

        // Check if order is DELIVERED
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can only review successfully delivered orders.");
        }

        // Check if already reviewed
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("You have already reviewed this order.");
        }

        Restaurant restaurant = order.getRestaurant();

        Review review = Review.builder()
                .customer(customer)
                .restaurant(restaurant)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update Restaurant Average Rating
        int newTotalReviews = restaurant.getTotalReviews() + 1;
        BigDecimal ratingValue = BigDecimal.valueOf(request.getRating());
        BigDecimal currentAvgSum = restaurant.getAvgRating().multiply(BigDecimal.valueOf(restaurant.getTotalReviews()));
        BigDecimal newAvgRating = currentAvgSum.add(ratingValue)
                .divide(BigDecimal.valueOf(newTotalReviews), 1, RoundingMode.HALF_UP);

        restaurant.setTotalReviews(newTotalReviews);
        restaurant.setAvgRating(newAvgRating);
        restaurantRepository.save(restaurant);

        log.info("Review created successfully with ID: {}", savedReview.getId());
        return reviewMapper.toResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getRestaurantReviews(Long restaurantId, Pageable pageable) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(reviewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getCustomerReviews(String email, Pageable pageable) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId(), pageable)
                .map(reviewMapper::toResponse);
    }

    @Transactional
    public ReviewResponse replyToReview(String ownerEmail, Long reviewId, ReviewReplyRequest request) {
        log.info("Replying to review ID: {} by owner: {}", reviewId, ownerEmail);

        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getRestaurant().getId().equals(restaurant.getId())) {
            throw new ForbiddenException("You do not have permission to reply to this review.");
        }

        review.setReply(request.getReply());
        review.setRepliedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        log.info("Reply added successfully to review ID: {}", reviewId);

        return reviewMapper.toResponse(savedReview);
    }
}
