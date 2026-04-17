package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@Tag(name = "Wallet Management", description = "APIs for managing user wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Create a new wallet for a user",
            description = "Creates a new wallet for the specified user ID with a zero balance.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Wallet created successfully",
                            content = @Content(schema = @Schema(implementation = Wallet.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWalletForUser(request.getUserId());
        return ResponseEntity.ok(wallet);
    }
    // API to deposit money into a wallet
    @Operation(
            summary = "Deposit amount to wallet", // Short description for Swagger UI
            description = "This API adds a given amount to the wallet balance", // Detailed explanation
            responses = {
                    @ApiResponse(responseCode = "200", description = "Amount deposited successfully"), // Success case
                    @ApiResponse(responseCode = "404", description = "Wallet not found") // If wallet doesn't exist
            }
    )
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<Wallet> deposit(
            @PathVariable UUID walletId,
            @RequestParam BigDecimal amount) {

        // Perform deposit
        walletService.deposit(walletId, amount);

        // Fetch updated wallet
        Wallet updatedWallet = walletService.getWalletById(walletId);

        // Return updated wallet
        return ResponseEntity.ok(updatedWallet);
    }

    @Operation(
            summary = "Withdraw amount from wallet",
            description = "Deducts specified amount from the wallet balance if sufficient funds are available",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Amount withdrawn successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance"),
                    @ApiResponse(responseCode = "404", description = "Wallet not found")
            }
    )
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @PathVariable UUID walletId,
            @RequestParam BigDecimal amount) {

        walletService.withdraw(walletId, amount);

        Wallet updatedWallet = walletService.getWalletById(walletId); // add this method

        return ResponseEntity.ok(updatedWallet);
    }
}