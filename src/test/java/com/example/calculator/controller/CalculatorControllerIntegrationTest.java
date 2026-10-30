package com.example.calculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculatorController.class)
public class CalculatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAddNumbers() throws Exception {
        mockMvc.perform(get("/api/v1/add?a=10&b=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.result").value(15));
    }

    @Test
    void shouldSubtractNumbers() throws Exception {
        mockMvc.perform(get("/api/v1/subtract?a=10&b=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("subtract"))
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    void shouldMultiplyNumbers() throws Exception {
        mockMvc.perform(get("/api/v1/multiply?a=10&b=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("multiply"))
                .andExpect(jsonPath("$.result").value(50));
    }

    @Test
    void shouldDivideNumbers() throws Exception {
        mockMvc.perform(get("/api/v1/divide?a=10&b=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("divide"))
                .andExpect(jsonPath("$.result").value(2));
    }

    @Test
    void shouldRejectDivisionByZero() throws Exception {
        mockMvc.perform(get("/api/v1/divide?a=10&b=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Division by zero is not allowed"));
    }

    @Test
    void shouldRejectMissingParameter() throws Exception {
        mockMvc.perform(get("/api/v1/add?a=10"))
                .andExpect(status().isBadRequest());
    }
}
