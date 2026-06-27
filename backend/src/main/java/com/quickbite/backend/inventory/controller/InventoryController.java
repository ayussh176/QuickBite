package com.quickbite.backend.inventory.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.inventory.dto.InventoryResponse;
import com.quickbite.backend.inventory.dto.UpdateStockRequest;
import com.quickbite.backend.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/v1/restaurants/inventory")
@PreAuthorize("hasRole('RESTAURANT')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Inventory Controller", description = "Endpoints for merchant stock management and inventory tracking")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Get full restaurant inventory", description = "Retrieves a paginated list of all menu items and their current stock levels for the authenticated restaurant owner")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getInventory(Pageable pageable, Principal principal) {
        log.info("Inventory fetch requested by merchant: {}", principal.getName());
        Page<InventoryResponse> response = inventoryService.getInventory(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Inventory levels fetched successfully.", response));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock warnings", description = "Fetches a paginated list of items that fall below their configured low-stock thresholds")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getLowStock(Pageable pageable, Principal principal) {
        log.info("Low stock warnings requested by merchant: {}", principal.getName());
        Page<InventoryResponse> response = inventoryService.getLowStockItems(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Low stock items fetched successfully.", response));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item stock level", description = "Allows updating current quantity and threshold values for a specific food item")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(@PathVariable Long itemId,
                                                                      @Valid @RequestBody UpdateStockRequest request,
                                                                      Principal principal) {
        log.info("Stock update for item ID: {} requested by merchant: {}", itemId, principal.getName());
        InventoryResponse response = inventoryService.updateStock(principal.getName(), itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Stock levels updated successfully.", response));
    }
}
