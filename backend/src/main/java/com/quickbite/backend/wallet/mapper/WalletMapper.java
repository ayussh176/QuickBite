package com.quickbite.backend.wallet.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.wallet.dto.WalletResponse;
import com.quickbite.backend.wallet.dto.WalletTransactionResponse;
import com.quickbite.backend.wallet.entity.Wallet;
import com.quickbite.backend.wallet.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface WalletMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    WalletResponse toResponse(Wallet wallet);

    WalletTransactionResponse toTransactionResponse(WalletTransaction transaction);
}
