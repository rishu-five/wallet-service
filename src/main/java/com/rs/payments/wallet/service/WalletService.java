package com.rs.payments.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;
import com.rs.payments.wallet.model.Wallet;

public interface WalletService {
    Wallet createWalletForUser(UUID userId);
    void deposit(UUID walletId, BigDecimal amount);
    void withdraw(UUID walletId, BigDecimal amount);
    Wallet getWalletById(UUID walletId);
}