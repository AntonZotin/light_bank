package com.bank.light.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UndoDto {
    @NotNull(message = "TransactionId can't be null")
    private Long transactionId;
}
