
package com.eaglebank.controller;

import com.eaglebank.security.JwtTokenProvider;
import com.eaglebank.service.AuthService;
import com.eaglebank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @MockBean private AuthService authService;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;


    @Test
    void createUser201() throws Exception {
        String body = "{\"name\":\"Mock User\",\"address\":{\"line1\":\"1 St\",\"town\":\"Town\",\"county\":\"County\",\"postcode\":\"AB1 2CD\"},\"phoneNumber\":\"+447700900000\",\"email\":\"mock@example.com\",\"password\":\"Pass@123\"}";
        Mockito.when(userService.createUser(Mockito.any())).thenAnswer(inv -> {
            com.eaglebank.model.User u = (com.eaglebank.model.User) inv.getArgument(0);
            u.setId("usr-abc123");
            return u;
        });
        mockMvc.perform(post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(body))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value("usr-abc123"));
    }
    @Test
    void login200() throws Exception {
        Mockito.when(authService.authenticate(Mockito.eq("mock@example.com"), Mockito.eq("Pass@123"))).thenReturn("jwt");
        String body = "{\"email\":\"mock@example.com\",\"password\":\"Pass@123\"}";
        mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").value("jwt"));
    }

}
