package com.eaglebank.dto.requests;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateUserRequest {
    @NotBlank
    private String name;

    @NotNull
    private AddressDto address;

    @NotBlank
    private String phoneNumber;

    @Email(message = "Email must be valid")
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @Data
    public static class AddressDto {
        @NotBlank private String line1;
        private String line2;
        private String line3;
        @NotBlank private String town;
        @NotBlank private String county;
        @NotBlank private String postcode;
    }
}
