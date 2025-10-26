package com.eaglebank.dto.requests;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    @NotNull private BigDecimal amount;
    @NotNull private String currency;
    @NotNull private String type; // deposit|withdrawal
    private String reference;
}
