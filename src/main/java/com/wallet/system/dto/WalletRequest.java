package com.wallet.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {

    @NotBlank(message = "Owner name is required")
    private String ownerName;
}