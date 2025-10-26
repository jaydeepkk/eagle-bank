package com.eaglebank.dto.requests;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CreateBankAccountRequest {
    @NotBlank private String name;
    @NotBlank private String accountType;
}
