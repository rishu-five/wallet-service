package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.Transaction;
import com.rs.payments.wallet.model.TransactionType;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import com.rs.payments.wallet.service.WalletService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(UserRepository userRepository, WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Wallet createWalletForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);
        user.setWallet(wallet);

        user = userRepository.save(user); // Cascade saves wallet
        return user.getWallet();
    }
    @Override
    public void deposit(UUID walletId, BigDecimal amount) {

        // 1. Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // 2. Fetch wallet
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        // 3. Update balance
        wallet.setBalance(wallet.getBalance().add(amount));

        // 4. Save wallet
        walletRepository.save(wallet);
        Transaction transaction = new Transaction();
        // Link this transaction to the wallet
        transaction.setWallet(wallet);
        // Set the amount that was deposited
        transaction.setAmount(amount);

        // Set transaction type as DEPOSIT which can also be WITHDRAW in future
        transaction.setType(TransactionType.DEPOSIT);

        // Save transaction in database to persist record for history/audit
        transactionRepository.save(transaction);

    }

    @Override
    public void withdraw(UUID walletId, BigDecimal amount) {

        // 1. Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // 2. Fetch wallet
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        // 3. Check sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // 4. Deduct amount
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // 5. Create transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAWAL);

        transactionRepository.save(transaction);
    }

    @Override
    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }
}