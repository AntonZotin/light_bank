package com.bank.light.models;

import com.bank.light.security.validators.DoubleType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferDto {
    @NotNull(message = "Amount can't be null")
    @NotEmpty(message = "Amount can't be empty")
    @Positive(message = "Amount can't be negative")
    @DoubleType
    private String amount;

    @NotNull(message = "Username can't be null")
    @NotEmpty(message = "Username can't be empty")
    private String username;
}
