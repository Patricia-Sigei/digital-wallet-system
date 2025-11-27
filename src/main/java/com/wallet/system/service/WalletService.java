package com.wallet.system.service;

import com.wallet.system.dto.BalanceUpdateRequest;
import com.wallet.system.dto.WalletRequest;
import com.wallet.system.dto.WalletResponse;
import com.wallet.system.entity.Wallet;
import com.wallet.system.exception.InsufficientBalanceException;
import com.wallet.system.exception.WalletAlreadyExistsException;
import com.wallet.system.exception.WalletNotFoundException;
import com.wallet.system.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public WalletResponse createWallet(WalletRequest request) {
        String walletId = "WALLET-" + UUID.randomUUID().toString();

        if (walletRepository.existsByWalletId(walletId)) {
            throw new WalletAlreadyExistsException("Wallet ID already exists");
        }

        Wallet wallet = new Wallet();
        wallet.setWalletId(walletId);
        wallet.setOwnerName(request.getOwnerName());
        wallet.setBalance(BigDecimal.ZERO);

        Wallet savedWallet = walletRepository.save(wallet);
        return mapToResponse(savedWallet);
    }

    public WalletResponse getWallet(String walletId) {
        Wallet wallet = walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        return mapToResponse(wallet);
    }

    @Transactional
    public WalletResponse updateBalance(String walletId, BalanceUpdateRequest request) {
        Wallet wallet = walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Current: " + wallet.getBalance());
        }

        wallet.setBalance(newBalance);
        Wallet updatedWallet = walletRepository.save(wallet);
        return mapToResponse(updatedWallet);
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getWalletId(),
                wallet.getOwnerName(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}