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
    @PostMapping("/{walletId}/deposit") // Endpoint: /wallets/{walletId}/deposit
    public ResponseEntity<String> deposit(
            @PathVariable UUID walletId, // Wallet ID coming from URL
            @RequestParam BigDecimal amount) { // Amount coming as query parameter

        // Call service layer to perform deposit logic
        walletService.deposit(walletId, amount);

        // Return success response to user
        return ResponseEntity.ok("Amount deposited successfully");
    }
}