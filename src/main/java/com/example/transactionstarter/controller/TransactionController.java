package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getTransactionId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable("id") String id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/transactions/{id}/status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateStatusRequest request) {
        TransactionResponse response = transactionService.updateTransactionStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{customerId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCustomerId(@PathVariable("customerId") String customerId) {
        List<TransactionResponse> responses = transactionService.getTransactionsByCustomerId(customerId);
        return ResponseEntity.ok(responses);
    }
}
