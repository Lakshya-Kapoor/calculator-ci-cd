package com.example.calculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    public record CalculatorResponse(String operation, double a, double b, double result) {
    }

    public record ErrorResponse(String error) {
    }

    @GetMapping("/add")
    public CalculatorResponse add(@RequestParam double a, @RequestParam double b) {
        double result = a + b;
        return new CalculatorResponse("add", a, b, result);
    }

    @GetMapping("/subtract")
    public CalculatorResponse subtract(@RequestParam double a, @RequestParam double b) {
        double result = a - b;
        return new CalculatorResponse("subtract", a, b, result);
    }

    @GetMapping("/multiply")
    public CalculatorResponse multiply(@RequestParam double a, @RequestParam double b) {
        double result = a * b;
        return new CalculatorResponse("multiply", a, b, result);
    }

    @GetMapping("/divide")
    public CalculatorResponse divide(@RequestParam double a, @RequestParam double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }

        double result = a / b;
        return new CalculatorResponse("divide", a, b, result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }
}
