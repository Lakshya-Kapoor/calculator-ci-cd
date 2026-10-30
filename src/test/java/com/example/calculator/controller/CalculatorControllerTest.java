package com.example.calculator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorControllerTest {
    private CalculatorController controller;

    @BeforeEach
    void setUp() {
        controller = new CalculatorController();
    }

    @Test
    void shouldAddNumbers() {
        var response = controller.add(10, 5);
        assertEquals("add", response.operation());
        assertEquals(10, response.a());
        assertEquals(5, response.b());
        assertEquals(15, response.result());
    }

    @Test
    void shouldSubtractNumbers() {
        var response = controller.subtract(10, 5);
        assertEquals("subtract", response.operation());
        assertEquals(10, response.a());
        assertEquals(5, response.b());
        assertEquals(5, response.result());
    }

    @Test
    void shouldMultiplyNumbers() {
        var response = controller.multiply(10, 5);
        assertEquals("multiply", response.operation());
        assertEquals(10, response.a());
        assertEquals(5, response.b());
        assertEquals(50, response.result());
    }

    @Test
    void shouldDivideNumbers() {
        var response = controller.divide(10, 5);
        assertEquals("divide", response.operation());
        assertEquals(10, response.a());
        assertEquals(5, response.b());
        assertEquals(2, response.result());
    }

    @Test
    void shouldModNumbers() {
        var response = controller.mod(10, 3);
        assertEquals("mod", response.operation());
        assertEquals(10, response.a());
        assertEquals(3, response.b());
        assertEquals(1, response.result());
    }

    @Test
    void shouldRejectDivisionByZero() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.divide(10, 0));

        assertEquals(
                "Division by zero is not allowed",
                exception.getMessage());

    }

    @Test
    void shouldRejectModulusByZero() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.mod(10, 0));

        assertEquals(
                "Modulus by zero is not allowed",
                exception.getMessage());
    }
}
