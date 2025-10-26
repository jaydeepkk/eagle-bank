
package com.eaglebank.controller;

import com.eaglebank.service.AccountService;
import com.eaglebank.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private TransactionService txService;
    @MockBean private AccountService accountService;


    @Test
    void createTransactionDeposit201() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("usr-abc123", null, java.util.List.of());
        com.eaglebank.model.User user = com.eaglebank.model.User.builder().id("usr-abc123").build();
        com.eaglebank.model.Account acc = com.eaglebank.model.Account.builder().accountNumber("01000001").owner(user).currency("GBP").build();
        Mockito.when(accountService.findByAccountNumber("01000001")).thenReturn(acc);
        com.eaglebank.model.Transaction tx = com.eaglebank.model.Transaction.builder().id("tan-abc123").type("deposit").amount(new java.math.BigDecimal("10.00")).currency("GBP").build();
        Mockito.when(txService.deposit(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.any())).thenReturn(tx);

        mockMvc.perform(post("/v1/accounts/01000001/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.00,\"currency\":\"GBP\",\"type\":\"deposit\"}")
                        .principal(auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("tan-abc123"));

    }

}
