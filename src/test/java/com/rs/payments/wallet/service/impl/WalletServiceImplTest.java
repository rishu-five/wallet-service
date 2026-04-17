package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.Transaction;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    @DisplayName("Should create wallet for existing user")
    void shouldCreateWalletForExistingUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // The service saves the user, which cascades to wallet. 
        // We mock save to return the user.
        when(userRepository.save(user)).thenReturn(user);

        // When
        Wallet result = walletService.createWalletForUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        // Verify interactions
        verify(userRepository, times(1)).findById(userId); // Called twice due to second assert
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.createWalletForUser(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deposit amount to wallet")
    void shouldDepositAmountToWallet() {
        // Given
        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));

        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet)).thenReturn(wallet);

        // When
        walletService.deposit(walletId, BigDecimal.valueOf(50));

        // Then
        assertEquals(BigDecimal.valueOf(150), wallet.getBalance());

        verify(walletRepository, times(1)).findById(walletId);
        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should withdraw amount from wallet")
    void shouldWithdrawAmountFromWallet() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));

        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.withdraw(walletId, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), wallet.getBalance());

        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient balance")
    void shouldFailWhenInsufficientBalance() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(30));

        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.withdraw(walletId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should transfer amount between wallets")
    void shouldTransferAmountBetweenWallets() {

        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromWalletId);
        fromWallet.setBalance(BigDecimal.valueOf(200));

        Wallet toWallet = new Wallet();
        toWallet.setId(toWalletId);
        toWallet.setBalance(BigDecimal.valueOf(100));

        when(walletRepository.findById(fromWalletId))
                .thenReturn(Optional.of(fromWallet));

        when(walletRepository.findById(toWalletId))
                .thenReturn(Optional.of(toWallet));

        walletService.transfer(fromWalletId, toWalletId, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), fromWallet.getBalance());
        assertEquals(BigDecimal.valueOf(150), toWallet.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
    
    @Test
    @DisplayName("Should fail transfer when insufficient balance")
    void shouldFailTransferWhenInsufficientBalance() {

        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromWalletId);
        fromWallet.setBalance(BigDecimal.valueOf(30));

        Wallet toWallet = new Wallet();
        toWallet.setId(toWalletId);

        when(walletRepository.findById(fromWalletId))
                .thenReturn(Optional.of(fromWallet));

        // 🔥 THIS WAS MISSING
        when(walletRepository.findById(toWalletId))
                .thenReturn(Optional.of(toWallet));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.transfer(fromWalletId, toWalletId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }

}
