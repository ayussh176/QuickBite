package com.quickbite.backend.wallet.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.wallet.dto.AddMoneyRequest;
import com.quickbite.backend.wallet.dto.CashbackRequest;
import com.quickbite.backend.wallet.dto.WalletResponse;
import com.quickbite.backend.wallet.dto.WalletTransactionResponse;
import com.quickbite.backend.wallet.service.WalletService;
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
@RequestMapping("/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet Controller", description = "Endpoints for checking wallet balance, adding money, viewing transaction histories, and crediting cashback")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get wallet details", description = "Retrieves wallet balance and account status for the logged-in customer")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(Principal principal) {
        log.info("Wallet retrieval requested by: {}", principal.getName());
        WalletResponse response = walletService.getWallet(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Wallet details fetched successfully.", response));
    }

    @PostMapping("/add-money")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add money to wallet", description = "Simulates adding funds to the customer wallet and logs a credit transaction")
    public ResponseEntity<ApiResponse<WalletResponse>> addMoney(@Valid @RequestBody AddMoneyRequest request,
                                                                Principal principal) {
        log.info("Rider/customer deposit requested by: {}", principal.getName());
        WalletResponse response = walletService.addMoney(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Money added successfully.", response));
    }

    @PostMapping("/cashback")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Credit cashback (Admin)", description = "Allows administrators to credit promotional cashbacks to customer wallets")
    public ResponseEntity<ApiResponse<WalletResponse>> creditCashback(@Valid @RequestBody CashbackRequest request) {
        log.info("Cashback credit requested by admin for customer ID: {}", request.getCustomerId());
        WalletResponse response = walletService.addCashback(request);
        return ResponseEntity.ok(ApiResponse.success("Cashback credited successfully.", response));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get transaction history", description = "Fetches a paginated history of all debit, credit, cashback, and refund transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getTransactions(Pageable pageable,
                                                                                        Principal principal) {
        log.info("Transaction history requested by: {}", principal.getName());
        Page<WalletTransactionResponse> response = walletService.getTransactions(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Transactions history fetched successfully.", response));
    }
}
