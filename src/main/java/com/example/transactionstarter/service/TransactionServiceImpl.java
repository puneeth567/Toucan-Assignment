package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.exception.DuplicateTransactionIdException;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;
import com.example.transactionstarter.exception.ResourceNotFoundException;
import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        String txId = request.getTransactionId();
        if (StringUtils.hasText(txId)) {
            txId = txId.trim();
            if (transactionRepository.existsByTransactionId(txId)) {
                throw new DuplicateTransactionIdException("Transaction ID already exists: " + txId);
            }
        } else {
            txId = "TX-" + UUID.randomUUID().toString();
        }

        Transaction transaction = new Transaction(
                txId,
                request.getCustomerId().trim(),
                request.getAmount(),
                request.getCurrency().trim().toUpperCase(),
                request.getTransactionType(),
                TransactionStatus.PENDING,
                request.getDescription() != null ? request.getDescription().trim() : null
        );

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
        return TransactionResponse.fromEntity(transaction);
    }

    @Override
    public TransactionResponse updateTransactionStatus(String transactionId, UpdateStatusRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));

        TransactionStatus currentStatus = transaction.getTransactionStatus();
        TransactionStatus newStatus = request.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition transaction status from %s to %s", currentStatus, newStatus)
            );
        }

        transaction.setTransactionStatus(newStatus);
        Transaction updated = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByCustomerId(String customerId) {
        if (!StringUtils.hasText(customerId)) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
        return transactionRepository.findByCustomerId(customerId.trim())
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();
    }
}
