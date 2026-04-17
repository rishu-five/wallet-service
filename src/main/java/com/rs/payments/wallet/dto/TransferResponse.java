package com.rs.payments.wallet.dto;

import com.rs.payments.wallet.model.Wallet;

public class TransferResponse {

    private Wallet fromWallet;
    private Wallet toWallet;
    private String message;

    public TransferResponse(Wallet fromWallet, Wallet toWallet, String message) {
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.message = message;
    }

    public Wallet getFromWallet() {
        return fromWallet;
    }

    public Wallet getToWallet() {
        return toWallet;
    }

    public String getMessage() {
        return message;
    }
}