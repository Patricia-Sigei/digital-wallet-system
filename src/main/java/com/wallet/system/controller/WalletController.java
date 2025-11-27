package com.wallet.system.controller;

import com.wallet.system.dto.BalanceUpdateRequest;
import com.wallet.system.dto.WalletRequest;
import com.wallet.system.dto.WalletResponse;
import com.wallet.system.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody WalletRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String walletId) {
        WalletResponse response = walletService.getWallet(walletId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{walletId}/balance")
    public ResponseEntity<WalletResponse> updateBalance(
            @PathVariable String walletId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        WalletResponse response = walletService.updateBalance(walletId, request);
        return ResponseEntity.ok(response);
    }
}