
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

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AccountService accountService;
    @MockBean private TransactionService txService;


    @Test
    void createAccount201() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("usr-abc123", null, java.util.List.of());
        com.eaglebank.model.Account acc = com.eaglebank.model.Account.builder().accountNumber("01000001").name("A").accountType("personal").currency("GBP").build();
        Mockito.when(accountService.createAccount(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(acc);
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"accountType\":\"personal\"}")
                        .principal(auth))
                .andExpect(status().isCreated());
    }

}
