package com.quickbite.backend.wallet.service;

import com.quickbite.backend.common.enums.TransactionType;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.wallet.dto.*;
import com.quickbite.backend.wallet.entity.Wallet;
import com.quickbite.backend.wallet.entity.WalletTransaction;
import com.quickbite.backend.wallet.mapper.WalletMapper;
import com.quickbite.backend.wallet.repository.WalletRepository;
import com.quickbite.backend.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    private final WalletMapper walletMapper;
    private final com.quickbite.backend.common.service.CacheService cacheService;

    @Transactional
    @org.springframework.cache.annotation.Cacheable(value = "wallets", key = "#email")
    public WalletResponse getWallet(String email) {
        log.info("Loading wallet for customer: {}", email);
        Customer customer = getCustomerByEmail(email);
        Wallet wallet = getOrCreateWallet(customer);
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse addMoney(String email, AddMoneyRequest request) {
        log.info("Adding ₹{} to wallet for: {}", request.getAmount(), email);
        Customer customer = getCustomerByEmail(email);
        Wallet wallet = getOrCreateWallet(customer);

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .transactionType(TransactionType.CREDIT)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Money added to wallet")
                .referenceId(UUID.randomUUID().toString())
                .balanceAfter(savedWallet.getBalance())
                .build();
        transactionRepository.save(transaction);
        cacheService.evictWallet(email);

        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public WalletResponse addCashback(CashbackRequest request) {
        log.info("Crediting ₹{} cashback to customer ID: {}", request.getAmount(), request.getCustomerId());
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Wallet wallet = getOrCreateWallet(customer);
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .transactionType(TransactionType.CREDIT)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Promotional cashback credited")
                .referenceId(UUID.randomUUID().toString())
                .balanceAfter(savedWallet.getBalance())
                .build();
        transactionRepository.save(transaction);
        if (customer.getUser() != null) {
            cacheService.evictWallet(customer.getUser().getEmail());
        }

        return walletMapper.toResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> getTransactions(String email, Pageable pageable) {
        Customer customer = getCustomerByEmail(email);
        Wallet wallet = getOrCreateWallet(customer);

        return transactionRepository.findByWalletId(wallet.getId(), pageable)
                .map(walletMapper::toTransactionResponse);
    }

    // ==================== Helpers ====================

    private Customer getCustomerByEmail(String email) {
        return customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    private Wallet getOrCreateWallet(Customer customer) {
        return walletRepository.findByUserId(customer.getUser().getId())
                .orElseGet(() -> {
                    Wallet wallet = Wallet.builder()
                            .user(customer.getUser())
                            .balance(BigDecimal.ZERO)
                            .build();
                    return walletRepository.save(wallet);
                });
    }
}
