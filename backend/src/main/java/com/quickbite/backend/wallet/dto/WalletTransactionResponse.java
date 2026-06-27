package com.quickbite.backend.wallet.dto;

import com.quickbite.backend.common.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {

    private Long id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private String referenceId;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
